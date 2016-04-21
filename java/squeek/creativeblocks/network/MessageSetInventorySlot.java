package squeek.creativeblocks.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MessageSetInventorySlot implements IMessage, IMessageHandler<MessageSetInventorySlot, IMessage>
{
    byte slot;
    ItemStack itemStack;

    public MessageSetInventorySlot()
    {
    }

    public MessageSetInventorySlot(byte slot, ItemStack itemStack)
    {
        this.slot = slot;
        this.itemStack = itemStack;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.slot = buf.readByte();
        this.itemStack = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte(slot);
        ByteBufUtils.writeItemStack(buf, itemStack);
    }

    @Override
    public IMessage onMessage(MessageSetInventorySlot message, MessageContext ctx)
    {
        if (ctx.side == Side.SERVER && message.slot <= 35)
        {
            ctx.getServerHandler().playerEntity.inventory.setInventorySlotContents(message.slot, message.itemStack);
        }
        return null;
    }

}
