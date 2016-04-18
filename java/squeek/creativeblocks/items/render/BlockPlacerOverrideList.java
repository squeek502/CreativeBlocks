package squeek.creativeblocks.items.render;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import squeek.creativeblocks.items.ItemCreativeBlockPlacer;

import java.util.List;

public class BlockPlacerOverrideList extends ItemOverrideList
{
    public BlockPlacerOverrideList(List<ItemOverride> overridesIn)
    {
        super(ImmutableList.<ItemOverride>of());
    }

    @Override
    public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
    {
        if (stack.getItem() instanceof ItemCreativeBlockPlacer)
        {
            ItemStack pickedBlock = ((ItemCreativeBlockPlacer) stack.getItem()).getBlock(stack);
            return Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(pickedBlock != null ? pickedBlock : new ItemStack(Blocks.stone));
        }
        return originalModel;
    }
}
