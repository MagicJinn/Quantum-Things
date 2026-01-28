package lumien.randomthings.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import io.netty.buffer.ByteBuf;

/**
 * A message to read/write using {@link PacketBuffer} as a wrapper.
 */
public interface IRTMessage extends IMessage
{
    @Override
    default void fromBytes(ByteBuf buf)
    {
        readPacketData(new PacketBuffer(buf));
    }

    @Override
    default void toBytes(ByteBuf buf)
    {
        writePacketData(new PacketBuffer(buf));
    }

    void readPacketData(PacketBuffer buf);

    void writePacketData(PacketBuffer buf);
}
