package lumien.randomthings.network.magicavoxel;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import lumien.randomthings.handler.magicavoxel.ServerModelLibrary;
import lumien.randomthings.network.ServerboundMessage;

public class MessageModelRequest implements ServerboundMessage
{
	private String modelName;

	public MessageModelRequest() {}

	public MessageModelRequest(String modelName)
	{
		this.modelName = modelName;
	}

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        modelName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        ByteBufUtils.writeUTF8String(buf, modelName);
    }

    @Override
    public void handleOnServer(@Nonnull EntityPlayerMP player)
    {
        ServerModelLibrary.getInstance().requestModel(player.connection, modelName);
    }

    public static class Handler extends NoReplyHandler<MessageModelRequest> {}
}
