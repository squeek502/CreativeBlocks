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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
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
import squeek.creativeblocks.config.Config;
import squeek.creativeblocks.config.CreativeBlocksRegistry;
import squeek.creativeblocks.config.JSONConfigHandler;
import squeek.creativeblocks.items.ItemCreativeBlockPlacer;
import squeek.creativeblocks.network.MessageSetInventorySlot;
import squeek.creativeblocks.network.NetworkHandler;
import squeek.creativeblocks.proxy.ServerProxy;

import java.io.File;
import java.util.OptionalInt;
import java.util.function.Predicate;
import java.util.stream.IntStream;

@Mod(modid = ModInfo.MODID, version = ModInfo.VERSION)
public class CreativeBlocks
{
    public static final Logger log = LogManager.getLogger(ModInfo.MODID);

    @Mod.Instance(ModInfo.MODID)
    public static CreativeBlocks instance;
    public File sourceFile;

    @SidedProxy(clientSide = ModInfo.CLIENT_PROXY, serverSide = ModInfo.SERVER_PROXY)
    public static ServerProxy proxy;

    public static Item creativeBlockPlacer;
    public static ResourceLocation blockPlacerResourceLocation = new ResourceLocation(ModInfo.MODID_LOWER, "creativeBlockPlacer");
    public static ModelResourceLocation blockPlacerModelResouceLocation = new ModelResourceLocation(ModInfo.MODID_LOWER, "creativeBlockPlacer");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        creativeBlockPlacer = new ItemCreativeBlockPlacer();
        GameRegistry.register(creativeBlockPlacer, blockPlacerResourceLocation);
        proxy.preInit(event);
        Config.preInit(event);
        sourceFile = event.getSourceFile();
        MinecraftForge.EVENT_BUS.register(this);

        JSONConfigHandler.setup(event.getModConfigurationDirectory());
        CreativeBlocksRegistry.init();
        NetworkHandler.init();


        FMLInterModComms.sendMessage("Waila", "register", "squeek.creativeblocks.integration.waila.WailaRegistrar.register");
        FMLInterModComms.sendMessage("VersionChecker", "addVersionCheck", "http://www.ryanliptak.com/minecraft/versionchecker/squeek502/CreativeBlocks");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        proxy.postInit(event);
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
        ItemStack pickedBlock = null;

        if (target.typeOfHit == RayTraceResult.Type.MISS)
        {
            // Remove the held item if it's a Fabricator
            if (Config.pickBlockAirRemovesFabricator && player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() == creativeBlockPlacer)
            {
                NetworkHandler.channel.sendToServer(new MessageSetInventorySlot((byte) player.inventory.currentItem, null));
            }
            return false;
        }

        if (target.typeOfHit == RayTraceResult.Type.BLOCK)
        {
            BlockPos pos = target.getBlockPos();
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (!block.isAir(state, world, pos))
            {
                ItemStack pickBlock = block.getPickBlock(state, target, world, pos, player);

                if (isCreativeBlock(pickBlock))
                {
                    pickedBlock = pickBlock;
                }
            }
        }
        if (pickedBlock == null) // If pick block is not a creative block, fallback to vanilla code
            return false;

        for (int i = 0; i < player.inventory.mainInventory.length; i++)
        {
            ItemStack currentStack = player.inventory.getStackInSlot(i);

            if (currentStack != null && currentStack.getItem() == creativeBlockPlacer)
            {
                ItemCreativeBlockPlacer placer = (ItemCreativeBlockPlacer) currentStack.getItem();
                ItemStack stackInBlockPlacer = placer.getBlock(currentStack);

                // Check if pick block is equal to the block stored inside the block placer
                if (stackInBlockPlacer != null && stackInBlockPlacer.isItemEqual(pickedBlock))
                {
                    // Check if player has Fabricator with the "picked" block in it in the hotbar, if true, move to slot
                    if (i >= 0 && i <= 8) // 0-8 Hotbar slots
                    {
                        ItemStack stack = player.inventory.getStackInSlot(i);
                        if (stack != null && stack.getItem() == creativeBlockPlacer)
                        {
                            ItemCreativeBlockPlacer blockPlacer = (ItemCreativeBlockPlacer) stack.getItem();
                            if (blockPlacer.getBlock(stack).isItemEqual(pickedBlock))
                            {
                                player.inventory.currentItem = i;
                                return true;
                            }
                        }
                    }

                    // Move block placer from main inventory to the player's hotbar
                    if (i >= 9 && i <= 35) // 9-35 Main inventory slots
                    {
                        ItemCreativeBlockPlacer blockPlacer = (ItemCreativeBlockPlacer) currentStack.getItem();
                        if (blockPlacer.getBlock(currentStack).isItemEqual(pickedBlock))
                        {
                            int hotbarSlot = evaluateHotbar(player.inventory, is -> is == null).orElse(-1); // Get the first hotbar slot that is empty
                            if (hotbarSlot != -1) // Move the Fabricator from main inventory to the hotbar
                            {
                                player.inventory.currentItem = hotbarSlot;
                                NetworkHandler.channel.sendToServer(new MessageSetInventorySlot((byte) i, null));
                                NetworkHandler.channel.sendToServer(new MessageSetInventorySlot((byte) hotbarSlot, currentStack));
                                return true;
                            }
                            else // If no empty slots were found in the hotbar, we swap the Fabricator in the main inventory with the item in our hand
                            {
                                ItemStack toHotbar = currentStack;
                                ItemStack toMain = player.inventory.getStackInSlot(player.inventory.currentItem);
                                int toHotbarSlot = player.inventory.currentItem;
                                NetworkHandler.channel.sendToServer(new MessageSetInventorySlot((byte) i, toMain));
                                NetworkHandler.channel.sendToServer(new MessageSetInventorySlot((byte) toHotbarSlot, toHotbar));
                                return true;
                            }
                        }
                    }
                }
            }
        }

        // If player does not have the block he tried to pick in his inventory at all;
        // check player's hand if it's empty and give the player a Fabricator with the picked block in
        if (player.getHeldItemMainhand() == null)
        {
            ItemStack blockPlacer = ItemCreativeBlockPlacer.createItemStack(pickedBlock);
            NetworkHandler.channel.sendToServer(new MessageSetInventorySlot((byte) player.inventory.currentItem, blockPlacer));
            return true;
        }

        // If player's hand is not empty, get the first empty slot in the player's
        // hotbar, and give the player a new Fabricator with the picked block in
        int hotbarSlot = evaluateHotbar(player.inventory, stack -> stack == null).orElse(-1); /*eval(player.inventory, 0, 8, stack -> stack == null).orElse(-1);*/
        if (hotbarSlot != -1)
        {
            ItemStack stack = ItemCreativeBlockPlacer.createItemStack(pickedBlock);
            player.inventory.currentItem = hotbarSlot;
            NetworkHandler.channel.sendToServer(new MessageSetInventorySlot((byte) hotbarSlot, stack));
            return true;
        }

        // If the player doesn't have any empty slots in h*s hotbar, get the first empty
        // slot in the player's inventory, move the items in h*s hand to the empty slot
        // and give the player a new Fabricator and put it in the hotbar slot we just cleared
        int mainInventoryslot = evaluateInventory(player.inventory, stack -> stack == null).orElse(-1);
        if (mainInventoryslot != -1)
        {
            ItemStack fabricator = ItemCreativeBlockPlacer.createItemStack(pickedBlock);
            ItemStack toMain = player.inventory.getCurrentItem();
            NetworkHandler.channel.sendToServer(new MessageSetInventorySlot((byte) mainInventoryslot, toMain));
            NetworkHandler.channel.sendToServer(new MessageSetInventorySlot((byte) player.inventory.currentItem, fabricator));
            return true;
        }

        // If all the above is false, find the first Fabricator in the player's hotbar with _any_ block in it and
        // change that block to the block we have "picked" on. // TODO: Config?
        // This does not replace a Vanilla block with the "picked" block. It _MUST_ be a Fabricator. // TODO: Config?
        hotbarSlot = evaluateHotbar(player.inventory, stack -> stack != null && stack.getItem() == creativeBlockPlacer).orElse(-1);
        if (hotbarSlot != -1)
        {
            ItemStack stack = player.inventory.getStackInSlot(hotbarSlot);
            ((ItemCreativeBlockPlacer) stack.getItem()).setBlock(stack, pickedBlock);
            player.inventory.currentItem = hotbarSlot;
            NetworkHandler.channel.sendToServer(new MessageSetInventorySlot((byte) hotbarSlot, stack));
            return true;
        }

        // If all the above is false, find the first Fabricator in the player's main inventory with _any_ block in it and
        // change the block in it to the "picked" block. Now we swap it with the current item we're holding. // TODO: Config?
        // This does not replace a Vanilla block with the "picked" block. It _MUST_ be a Fabricator. // TODO: Config?
        mainInventoryslot = evaluateInventory(player.inventory, stack -> stack != null && stack.getItem() == creativeBlockPlacer).orElse(-1);
        if (mainInventoryslot != -1)
        {
            ItemStack blockPlacer = player.inventory.getStackInSlot(mainInventoryslot);
            ((ItemCreativeBlockPlacer) blockPlacer.getItem()).setBlock(blockPlacer, pickedBlock);
            ItemStack hotbarItem = player.inventory.getStackInSlot(player.inventory.currentItem);
            NetworkHandler.channel.sendToServer(new MessageSetInventorySlot((byte) mainInventoryslot, hotbarItem));
            NetworkHandler.channel.sendToServer(new MessageSetInventorySlot((byte) player.inventory.currentItem, blockPlacer));
            return true;
        }
        return false; // None of the above matches (inventory is completely full), fall back to vanilla logic. Replaces current item with vanilla blocks if in creative
    }

    public static OptionalInt eval(InventoryPlayer inventory, int minInclusive, int maxInclusive, Predicate<ItemStack> predicate)
    {
        return eval(inventory, IntStream.range(minInclusive, ++maxInclusive), predicate);
    }

    public static OptionalInt eval(InventoryPlayer inventory, IntStream range, Predicate<ItemStack> predicate)
    {
        return range.filter(slot -> predicate.test(inventory.getStackInSlot(slot))).findFirst();
    }

    public static OptionalInt evaluateHotbar(InventoryPlayer inventory, Predicate<ItemStack> predicate)
    {
        return eval(inventory, 0, 8, predicate);
    }

    public static OptionalInt evaluateInventory(InventoryPlayer inventory, Predicate<ItemStack> predicate)
    {
        return eval(inventory, 9, 35, predicate);
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