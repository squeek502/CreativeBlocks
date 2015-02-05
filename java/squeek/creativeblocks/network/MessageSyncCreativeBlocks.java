package squeek.creativeblocks.network;

import io.netty.buffer.ByteBuf;
import squeek.creativeblocks.config.BlocksSpecifier;
import squeek.creativeblocks.config.CreativeBlocksRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MessageSyncCreativeBlocks implements IMessage, IMessageHandler<MessageSyncCreativeBlocks, IMessage>
{
	boolean isWhitelist;
	BlocksSpecifier blocksSpecifier;

	public MessageSyncCreativeBlocks()
	{
	}

	public MessageSyncCreativeBlocks(BlocksSpecifier blocksSpecifier, boolean isWhitelist)
	{
		this.blocksSpecifier = blocksSpecifier;
		this.isWhitelist = isWhitelist;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		blocksSpecifier.pack(buf);
		buf.writeBoolean(isWhitelist);
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		blocksSpecifier = new BlocksSpecifier();
		blocksSpecifier.unpack(buf);
		isWhitelist = buf.readBoolean();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(MessageSyncCreativeBlocks message, MessageContext ctx)
	{
		if (message.isWhitelist)
			CreativeBlocksRegistry.whitelists.add(message.blocksSpecifier);
		else
			CreativeBlocksRegistry.blacklists.add(message.blocksSpecifier);

		return null;
	}
}
