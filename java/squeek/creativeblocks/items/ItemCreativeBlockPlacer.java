package squeek.creativeblocks.items;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import squeek.creativeblocks.CreativeBlocks;
import squeek.creativeblocks.ModInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemCreativeBlockPlacer extends Item
{
	public static final String NBT_KEY_BLOCK = "Block";

	public ItemCreativeBlockPlacer()
	{
		super();
		setMaxStackSize(1);
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		ItemStack blockToPlace = getBlock(itemStack);
		if (blockToPlace != null)
		{
			return blockToPlace.tryPlaceItemIntoWorld(player, world, x, y, z, side, hitX, hitY, hitZ);
		}
		else
			return super.onItemUse(itemStack, player, world, x, y, z, side, hitX, hitY, hitZ);
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemStack)
	{
		ItemStack pickedBlock = getBlock(itemStack);
		return super.getItemStackDisplayName(itemStack) + (pickedBlock != null ? ": " + pickedBlock.getDisplayName() : "");
	}

	public boolean hasBlock(ItemStack itemStack)
	{
		return getBlock(itemStack) != null;
	}

	public ItemStack getBlock(ItemStack itemStack)
	{
		if (!itemStack.hasTagCompound())
			return null;

		NBTTagCompound tag = itemStack.getTagCompound();

		if (!tag.hasKey(ModInfo.MODID))
			return null;

		NBTTagCompound modTag = tag.getCompoundTag(ModInfo.MODID);

		if (!modTag.hasKey(NBT_KEY_BLOCK))
			return null;

		NBTTagCompound blockTag = modTag.getCompoundTag(NBT_KEY_BLOCK);

		return getItemStackFromNBT(blockTag);
	}

	public boolean setBlock(ItemStack itemStack, ItemStack blockStack)
	{
		if (!CreativeBlocks.isCreativeBlock(blockStack))
			return false;

		if (!itemStack.hasTagCompound())
			itemStack.setTagCompound(new NBTTagCompound());

		NBTTagCompound tag = itemStack.getTagCompound();

		if (!tag.hasKey(ModInfo.MODID))
			tag.setTag(ModInfo.MODID, new NBTTagCompound());

		NBTTagCompound modTag = tag.getCompoundTag(ModInfo.MODID);

		if (!modTag.hasKey(NBT_KEY_BLOCK))
			modTag.setTag(NBT_KEY_BLOCK, new NBTTagCompound());

		NBTTagCompound blockTag = modTag.getCompoundTag(NBT_KEY_BLOCK);

		saveItemStackToNBT(blockTag, blockStack);
		return true;
	}

	public void clearBlock(ItemStack itemStack)
	{
		if (!itemStack.hasTagCompound())
			return;

		NBTTagCompound tag = itemStack.getTagCompound();

		if (!tag.hasKey(ModInfo.MODID))
			return;

		NBTTagCompound modTag = tag.getCompoundTag(ModInfo.MODID);

		if (!modTag.hasKey(NBT_KEY_BLOCK))
			return;

		modTag.setTag(NBT_KEY_BLOCK, null);
	}

	@SideOnly(Side.CLIENT)
	public ItemStack onPickBlock(ItemStack itemStack, MovingObjectPosition target, World world, EntityPlayer player)
	{
		if (target.typeOfHit == MovingObjectType.BLOCK)
		{
			int x = target.blockX;
			int y = target.blockY;
			int z = target.blockZ;
			Block block = world.getBlock(x, y, z);

			if (block.isAir(world, x, y, z))
			{
				return itemStack;
			}

			ItemStack blockStack = block.getPickBlock(target, world, x, y, z);

			if (setBlock(itemStack, blockStack))
			{
				player.swingItem();
				setBlock(itemStack, blockStack);
			}
		}
		else if (target.typeOfHit == MovingObjectType.MISS && hasBlock(itemStack))
		{
			player.swingItem();
			clearBlock(itemStack);
		}
		return itemStack;
	}

	public static ItemStack getItemStackFromNBT(NBTTagCompound tag)
	{
		return ItemStack.loadItemStackFromNBT(tag);
	}

	public static void saveItemStackToNBT(NBTTagCompound tag, ItemStack itemStack)
	{
		itemStack.writeToNBT(tag);
	}
}
