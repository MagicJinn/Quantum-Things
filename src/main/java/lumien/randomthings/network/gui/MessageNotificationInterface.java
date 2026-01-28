package lumien.randomthings.network.gui;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import lumien.randomthings.tileentity.TileEntityNotificationInterface;

public class MessageNotificationInterface extends TileGuiPacket<TileEntityNotificationInterface>
{
	private String newTitle;
	private String newDescription;

    public MessageNotificationInterface()
    {
        super();
    }

	public MessageNotificationInterface(String newTitle, String newDescription, BlockPos pos)
	{
        super(pos);
		this.newTitle = newTitle;
		this.newDescription = newDescription;
	}

    @Nonnull
    @Override
    protected Class<TileEntityNotificationInterface> getTileType()
    {
        return TileEntityNotificationInterface.class;
    }

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        super.readPacketData(buf);
        this.newTitle = ByteBufUtils.readUTF8String(buf);
        this.newDescription = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        super.writePacketData(buf);
        ByteBufUtils.writeUTF8String(buf, newTitle);
        ByteBufUtils.writeUTF8String(buf, newDescription);
    }

    @Override
    protected void onPacket(@Nonnull EntityPlayerMP player, TileEntityNotificationInterface tile)
    {
        tile.setData(newTitle, newDescription);
    }

    public static class Handler extends NoReplyHandler<MessageNotificationInterface> {}
}
