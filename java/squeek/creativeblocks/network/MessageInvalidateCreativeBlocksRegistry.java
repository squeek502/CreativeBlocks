package squeek.creativeblocks.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import squeek.creativeblocks.config.CreativeBlocksRegistry;

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
