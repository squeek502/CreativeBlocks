package squeek.creativeblocks.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

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
		if (ctx.side == Side.SERVER && message.slot < InventoryPlayer.getHotbarSize())
		{
			ctx.getServerHandler().playerEntity.inventory.setInventorySlotContents(message.slot, message.itemStack);
		}
		return null;
	}

}
