package squeek.creativeblocks.proxy;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import squeek.creativeblocks.CreativeBlocks;
import squeek.creativeblocks.items.render.CreativeBlocksModelLoader;

public class ClientProxy extends ServerProxy
{
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);
        ModelLoader.setCustomModelResourceLocation(CreativeBlocks.creativeBlockPlacer, 0, CreativeBlocks.blockPlacerModelResouceLocation);
        ModelLoaderRegistry.registerLoader(new CreativeBlocksModelLoader());
    }

    @Override
    public void init(FMLInitializationEvent event)
    {
        super.init(event);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event)
    {
        super.postInit(event);
    }
}
