package squeek.creativeblocks.items;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import squeek.creativeblocks.CreativeBlocks;
import squeek.creativeblocks.ModInfo;

public class ItemCreativeBlockPlacer extends Item
{
    public static final String NBT_KEY_BLOCK = "Block";

    public ItemCreativeBlockPlacer()
    {
        super();
        this.setUnlocalizedName(ModInfo.MODID + ".creativeBlockPlacer");
        setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ItemStack blockToPlace = getBlock(stack);

        if (blockToPlace != null)
        {
            return blockToPlace.onItemUse(playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
        }
        else
            return super.onItemUse(stack, playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
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
        if (itemStack == null)
        {
            CreativeBlocks.log.error("Tried to get block from ItemStack null");
            return null;
        }

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
    public ItemStack onPickBlock(ItemStack itemStack, RayTraceResult target, World world, EntityPlayer player)
    {
        if (target.typeOfHit == RayTraceResult.Type.BLOCK)
        {
            BlockPos pos = target.getBlockPos();
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (block.isAir(state, world, pos))
            {
                return itemStack;
            }

            ItemStack blockStack = block.getPickBlock(state, target, world, pos, player);

            if (setBlock(itemStack, blockStack))
            {
                player.swingArm(EnumHand.MAIN_HAND);
                setBlock(itemStack, blockStack);
            }
        }
        else if (target.typeOfHit == RayTraceResult.Type.MISS && hasBlock(itemStack))
        {
            player.swingArm(EnumHand.MAIN_HAND);
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

    @Override
    public boolean hasEffect(ItemStack stack)
    {
        return hasBlock(stack);
    }
}
