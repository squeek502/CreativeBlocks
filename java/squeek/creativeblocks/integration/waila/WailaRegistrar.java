package squeek.creativeblocks.integration.waila;

import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.block.Block;

public class WailaRegistrar
{
    public static void register(IWailaRegistrar registrar)
    {
        registrar.registerBodyProvider(new WailaProvider(), Block.class);
    }
}
