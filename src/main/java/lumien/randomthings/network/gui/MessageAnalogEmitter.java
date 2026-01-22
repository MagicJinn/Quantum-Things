package lumien.randomthings.network.gui;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import lumien.randomthings.container.ContainerAnalogEmitter;
import lumien.randomthings.tileentity.TileEntityAnalogEmitter;

public class MessageAnalogEmitter extends TileGuiPacket<TileEntityAnalogEmitter>
{
	private int level;

    public MessageAnalogEmitter()
    {
        super();
    }

	public MessageAnalogEmitter(BlockPos pos, int level)
	{
        super(pos);
		this.level = level;
	}

    @Nonnull
    @Override
    protected Class<TileEntityAnalogEmitter> getTileType()
    {
        return TileEntityAnalogEmitter.class;
    }

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        super.readPacketData(buf);
        level = buf.readVarInt();
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        super.writePacketData(buf);
        buf.writeVarInt(level);
    }

    @Override
    protected void onPacket(@Nonnull EntityPlayerMP player, TileEntityAnalogEmitter tile)
    {
        if (level <= 0 || level > 16) return;
        if (!(player.openContainer instanceof ContainerAnalogEmitter)) return;

        tile.setLevel(level);
    }

    public static class Handler extends NoReplyHandler<MessageAnalogEmitter> {}
}
