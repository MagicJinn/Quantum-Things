package lumien.randomthings.network.gui;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import lumien.randomthings.tileentity.TileEntityOnlineDetector;

public class MessageOnlineDetector extends TileGuiPacket<TileEntityOnlineDetector>
{
	private String username;

	public MessageOnlineDetector()
	{
        super();
	}

	public MessageOnlineDetector(String username, BlockPos pos)
	{
        super(pos);
		this.username = username;
	}

    @Nonnull
    @Override
    protected Class<TileEntityOnlineDetector> getTileType()
    {
        return TileEntityOnlineDetector.class;
    }

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        super.readPacketData(buf);
        username = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        super.writePacketData(buf);
        ByteBufUtils.writeUTF8String(buf, username);
    }

    @Override
    protected void onPacket(@Nonnull EntityPlayerMP player, TileEntityOnlineDetector tile)
    {
        if (pos.distanceSq(player.getPosition()) >= 100) return;

        tile.setUsername(username);
    }

    public static class Handler extends NoReplyHandler<MessageOnlineDetector> {}
}
