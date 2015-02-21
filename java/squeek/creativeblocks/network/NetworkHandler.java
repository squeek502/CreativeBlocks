package squeek.creativeblocks.network;

import squeek.creativeblocks.ModInfo;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class NetworkHandler
{
	public static final SimpleNetworkWrapper channel = NetworkRegistry.INSTANCE.newSimpleChannel(ModInfo.MODID);

	public static void init()
	{
		channel.registerMessage(MessageInvalidateCreativeBlocksRegistry.class, MessageInvalidateCreativeBlocksRegistry.class, 0, Side.CLIENT);
		channel.registerMessage(MessageSyncCreativeBlocks.class, MessageSyncCreativeBlocks.class, 1, Side.CLIENT);
		channel.registerMessage(MessageSetInventorySlot.class, MessageSetInventorySlot.class, 2, Side.SERVER);
	}
}
