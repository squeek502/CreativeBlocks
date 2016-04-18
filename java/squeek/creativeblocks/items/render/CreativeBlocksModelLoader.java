package squeek.creativeblocks.items.render;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import squeek.creativeblocks.CreativeBlocks;

public class CreativeBlocksModelLoader implements ICustomModelLoader
{
    @Override
    public void onResourceManagerReload(IResourceManager resourceManager)
    {
    }

    @Override
    public boolean accepts(ResourceLocation modelLocation)
    {
        return modelLocation.equals(CreativeBlocks.blockPlacerModelResouceLocation);
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception
    {
        return new BlockPlacerModel();
    }
}
