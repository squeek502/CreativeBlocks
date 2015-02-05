package squeek.creativeblocks.network;

import io.netty.buffer.ByteBuf;
import squeek.creativeblocks.config.CreativeBlocksRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MessageInvalidateCreativeBlocksRegistry implements IMessage, IMessageHandler<MessageInvalidateCreativeBlocksRegistry, IMessage>
{
	@SideOnly(Side.CLIENT)
	@Override
	public IMessage onMessage(MessageInvalidateCreativeBlocksRegistry message, MessageContext ctx)
	{
		CreativeBlocksRegistry.invalidate();
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
	}
}
