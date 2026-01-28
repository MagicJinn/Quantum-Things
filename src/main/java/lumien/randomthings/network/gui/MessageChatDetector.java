package lumien.randomthings.network.gui;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import lumien.randomthings.tileentity.TileEntityChatDetector;

public class MessageChatDetector extends TileGuiPacket<TileEntityChatDetector>
{
	private String chatMessage;
	private boolean consume;

	public MessageChatDetector()
	{
        super();
	}

	public MessageChatDetector(String chatMessage, boolean consume, BlockPos pos)
	{
        super(pos);
		this.chatMessage = chatMessage;
		this.consume = consume;
	}

    @Nonnull
    @Override
    protected Class<TileEntityChatDetector> getTileType()
    {
        return TileEntityChatDetector.class;
    }

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        super.readPacketData(buf);
        chatMessage = ByteBufUtils.readUTF8String(buf);
        consume = buf.readBoolean();
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        super.writePacketData(buf);
        ByteBufUtils.writeUTF8String(buf, chatMessage);
        buf.writeBoolean(consume);
    }

    @Override
    protected void onPacket(@Nonnull EntityPlayerMP player, TileEntityChatDetector tile)
    {
        tile.setChatMessage(chatMessage);
        tile.setConsume(consume);
    }

    public static class Handler extends NoReplyHandler<MessageChatDetector> {}
}
