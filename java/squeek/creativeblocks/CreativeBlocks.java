package squeek.creativeblocks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import squeek.creativeblocks.commands.Commands;
import squeek.creativeblocks.config.CreativeBlocksRegistry;
import squeek.creativeblocks.config.JSONConfigHandler;
import squeek.creativeblocks.items.ItemCreativeBlockPlacer;
import squeek.creativeblocks.items.render.CreativeBlocksModelLoader;
import squeek.creativeblocks.network.MessageSetInventorySlot;
import squeek.creativeblocks.network.NetworkHandler;

import java.io.File;

@Mod(modid = ModInfo.MODID, version = ModInfo.VERSION)
public class CreativeBlocks
{
    public static final Logger log = LogManager.getLogger(ModInfo.MODID);

    @Mod.Instance(ModInfo.MODID)
    public static CreativeBlocks instance;
    public File sourceFile;

    public static Item creativeBlockPlacer;
    public static ResourceLocation blockPlacerResourceLocation = new ResourceLocation(ModInfo.MODID_LOWER, "creativeBlockPlacer");
    public static ModelResourceLocation blockPlacerModelResouceLocation = new ModelResourceLocation(ModInfo.MODID_LOWER, "creativeBlockPlacer");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        sourceFile = event.getSourceFile();
        MinecraftForge.EVENT_BUS.register(this);

        JSONConfigHandler.setup(event.getModConfigurationDirectory());
        CreativeBlocksRegistry.init();
        NetworkHandler.init();

        creativeBlockPlacer = new ItemCreativeBlockPlacer();
        GameRegistry.register(creativeBlockPlacer, blockPlacerResourceLocation);

        ModelLoader.setCustomModelResourceLocation(creativeBlockPlacer, 0, blockPlacerModelResouceLocation);
        ModelLoaderRegistry.registerLoader(new CreativeBlocksModelLoader());

        FMLInterModComms.sendMessage("Waila", "register", "squeek.creativeblocks.integration.waila.WailaRegistrar.register");
        FMLInterModComms.sendMessage("VersionChecker", "addVersionCheck", "http://www.ryanliptak.com/minecraft/versionchecker/squeek502/CreativeBlocks");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        JSONConfigHandler.load();
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event)
    {
        Commands.init(event.getServer());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockDrops(BlockEvent.HarvestDropsEvent event)
    {
        IBlockState state = event.getState();
        Block block = state.getBlock();

        if (isCreativeBlock(block, block.damageDropped(state)))
            event.getDrops().clear();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void getTooltip(ItemTooltipEvent event)
    {
        if (!isCreativeBlock(event.getItemStack()))
            return;

        event.getToolTip().add(TextFormatting.LIGHT_PURPLE + TextFormatting.ITALIC.toString() + I18n.translateToLocal(ModInfo.MODID + ".tooltip"));
    }

    @SideOnly(Side.CLIENT)
    public static boolean onPickBlock()
    {
        Minecraft mc = Minecraft.getMinecraft();
        RayTraceResult target = mc.objectMouseOver;
        World world = mc.theWorld;
        EntityPlayer player = mc.thePlayer;
        ItemStack blockStack = null;

        if (target.typeOfHit == RayTraceResult.Type.MISS)
        {
            return false;
        }

        if (target.typeOfHit == RayTraceResult.Type.BLOCK)
        {
            BlockPos pos = target.getBlockPos();
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (!block.isAir(state, world, pos))
            {
                ItemStack pickedBlock = block.getPickBlock(state, target, world, pos, player);

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
        if (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() == creativeBlockPlacer)
        {
            blockPlacer = ((ItemCreativeBlockPlacer) player.getHeldItemMainhand().getItem()).onPickBlock(player.getHeldItemMainhand(), target, world, player);
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

    public static boolean isCreativeBlock(World world, BlockPos pos)
    {
        return isCreativeBlock(world.getBlockState(pos).getBlock(), world.getBlockState(pos));
    }

    public static boolean isCreativeBlock(Block block, IBlockState state)
    {
        return isCreativeBlock(block, block.getMetaFromState(state));
    }

    public static boolean isCreativeBlock(Block block, int metadata)
    {
        if (block == null)
            return false;

        return CreativeBlocksRegistry.isCreativeBlock(block, metadata);
    }
}
