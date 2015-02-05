package squeek.creativeblocks;

import java.io.File;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.BlockEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import squeek.creativeblocks.config.CreativeBlocksRegistry;
import squeek.creativeblocks.config.JSONConfigHandler;
import squeek.creativeblocks.network.NetworkHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = ModInfo.MODID, version = ModInfo.VERSION)
public class CreativeBlocks
{
	public static final Logger Log = LogManager.getLogger(ModInfo.MODID);

	@Instance(ModInfo.MODID)
	public static CreativeBlocks instance;
	public File sourceFile;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		sourceFile = event.getSourceFile();
		MinecraftForge.EVENT_BUS.register(this);

		JSONConfigHandler.setup(event.getModConfigurationDirectory());
		CreativeBlocksRegistry.init();
		NetworkHandler.init();

		FMLInterModComms.sendMessage("Waila", "register", "squeek.creativeblocks.integration.waila.WailaRegistrar.register");
		FMLInterModComms.sendMessage("VersionChecker", "addVersionCheck", "http://www.ryanliptak.com/minecraft/versionchecker/squeek502/CreativeBlocks");
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		JSONConfigHandler.load();
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
