package squeek.creativeblocks.network;

import io.netty.buffer.ByteBuf;

public interface IPackable
{
    void pack(ByteBuf data);

    void unpack(ByteBuf data);
}
