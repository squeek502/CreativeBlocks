package squeek.creativeblocks;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = ModInfo.MODID, version = ModInfo.VERSION)
public class CreativeBlocks
{
	public static final Logger Log = LogManager.getLogger(ModInfo.MODID);

	@Instance(ModInfo.MODID)
	public static CreativeBlocks instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onBlockDrops(BlockEvent.HarvestDropsEvent event)
	{
		if (isCreativeBlock(event.block, event.blockMetadata))
			event.drops.clear();
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

		if (block == Blocks.stonebrick)
			return true;

		return false;
	}
}