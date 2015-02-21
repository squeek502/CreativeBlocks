package squeek.creativeblocks;

import java.io.File;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.BlockEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import squeek.creativeblocks.commands.Commands;
import squeek.creativeblocks.config.CreativeBlocksRegistry;
import squeek.creativeblocks.config.JSONConfigHandler;
import squeek.creativeblocks.items.ItemCreativeBlockPlacer;
import squeek.creativeblocks.items.ItemCreativeBlockPlacerRenderer;
import squeek.creativeblocks.network.MessageSetInventorySlot;
import squeek.creativeblocks.network.NetworkHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = ModInfo.MODID, version = ModInfo.VERSION)
public class CreativeBlocks
{
	public static final Logger Log = LogManager.getLogger(ModInfo.MODID);

	@Instance(ModInfo.MODID)
	public static CreativeBlocks instance;
	public File sourceFile;

	public static Item creativeBlockPlacer;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		sourceFile = event.getSourceFile();
		MinecraftForge.EVENT_BUS.register(this);

		JSONConfigHandler.setup(event.getModConfigurationDirectory());
		CreativeBlocksRegistry.init();
		NetworkHandler.init();

		creativeBlockPlacer = new ItemCreativeBlockPlacer()
				.setUnlocalizedName(ModInfo.MODID + ".creativeBlockPlacer")
				.setTextureName(ModInfo.MODID_LOWER + ":creative_block_placer");
		GameRegistry.registerItem(creativeBlockPlacer, "creativeBlockPlacer");

		if (event.getSide() == Side.CLIENT)
		{
			MinecraftForgeClient.registerItemRenderer(creativeBlockPlacer, new ItemCreativeBlockPlacerRenderer());
		}

		FMLInterModComms.sendMessage("Waila", "register", "squeek.creativeblocks.integration.waila.WailaRegistrar.register");
		FMLInterModComms.sendMessage("VersionChecker", "addVersionCheck", "http://www.ryanliptak.com/minecraft/versionchecker/squeek502/CreativeBlocks");
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		JSONConfigHandler.load();
	}

	@EventHandler
	public void onServerStarting(FMLServerStartingEvent event)
	{
		Commands.init(event.getServer());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onBlockDrops(BlockEvent.HarvestDropsEvent event)
	{
		if (isCreativeBlock(event.block, event.blockMetadata))
			event.drops.clear();
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void getTooltip(ItemTooltipEvent event)
	{
		if (!isCreativeBlock(event.itemStack))
			return;

		event.toolTip.add(EnumChatFormatting.LIGHT_PURPLE + EnumChatFormatting.ITALIC.toString() + StatCollector.translateToLocal(ModInfo.MODID + ".tooltip"));
	}

	@SideOnly(Side.CLIENT)
	public static boolean onPickBlock()
	{
		Minecraft mc = Minecraft.getMinecraft();
		MovingObjectPosition target = mc.objectMouseOver;
		World world = mc.theWorld;
		EntityPlayer player = mc.thePlayer;
		ItemStack blockStack = null;

		if (target.typeOfHit == MovingObjectType.BLOCK)
		{
			int x = target.blockX;
			int y = target.blockY;
			int z = target.blockZ;
			Block block = world.getBlock(x, y, z);

			if (!block.isAir(world, x, y, z))
			{
				ItemStack pickedBlock = block.getPickBlock(target, world, x, y, z);

				if (isCreativeBlock(pickedBlock))
					blockStack = pickedBlock;
			}
		}

		if (blockStack != null)
		{
			for (int x = 0; x < InventoryPlayer.getHotbarSize(); x++)
			{
				ItemStack stack = player.inventory.getStackInSlot(x);

				if (stack != null && stack.getItem() == creativeBlockPlacer)
					stack = ((ItemCreativeBlockPlacer) stack.getItem()).getBlock(stack);

				if (stack != null && stack.isItemEqual(blockStack) && ItemStack.areItemStackTagsEqual(stack, blockStack))
				{
					player.inventory.currentItem = x;
					return true;
				}
			}
		}

		ItemStack blockPlacer;
		int slot;
		if (player.getHeldItem() != null && player.getHeldItem().getItem() == creativeBlockPlacer)
		{
			blockPlacer = ((ItemCreativeBlockPlacer) player.getHeldItem().getItem()).onPickBlock(player.getHeldItem(), target, world, player);
			slot = player.inventory.currentItem;
		}
		else if (blockStack != null)
		{
			slot = player.inventory.getFirstEmptyStack();
			if (slot < 0 || slot >= InventoryPlayer.getHotbarSize())
			{
				slot = player.inventory.currentItem;
			}

			blockPlacer = new ItemStack(creativeBlockPlacer);
			((ItemCreativeBlockPlacer) creativeBlockPlacer).setBlock(blockPlacer, blockStack);
		}
		else
		{
			return false;
		}

		player.inventory.setInventorySlotContents(slot, blockPlacer);
		player.inventory.currentItem = slot;
		NetworkHandler.channel.sendToServer(new MessageSetInventorySlot((byte) slot, blockPlacer));
		return true;
	}

	public static boolean isCreativeBlock(ItemStack itemStack)
	{
		if (itemStack == null || itemStack.getItem() == null)
			return false;

		Block blockFromItemStack = Block.getBlockFromItem(itemStack.getItem());

		if (blockFromItemStack == null)
			return false;

		return isCreativeBlock(blockFromItemStack, itemStack.getItemDamage());
	}

	public static boolean isCreativeBlock(World world, int x, int y, int z)
	{
		return isCreativeBlock(world.getBlock(x, y, z), world.getBlockMetadata(x, y, z));
	}

	public static boolean isCreativeBlock(Block block, int metadata)
	{
		if (block == null)
			return false;

		return CreativeBlocksRegistry.isCreativeBlock(block, metadata);
	}
}
