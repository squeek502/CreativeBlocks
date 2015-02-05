package squeek.creativeblocks.network;

import io.netty.buffer.ByteBuf;

public interface IPackable
{
	public void pack(ByteBuf data);
	public void unpack(ByteBuf data);
}
