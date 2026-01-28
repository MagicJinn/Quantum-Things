package lumien.randomthings.network.magicavoxel;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import lumien.randomthings.handler.magicavoxel.ClientModelLibrary;
import lumien.randomthings.network.ClientboundMessage;

public class MessageModelData implements ClientboundMessage
{
	private String modelName;
	private byte[] data;

	public MessageModelData() {}

	public MessageModelData(String modelName, byte[] data)
	{
		this.modelName = modelName;
		this.data = data;
	}

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        modelName = ByteBufUtils.readUTF8String(buf);
        data = buf.readByteArray();
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        ByteBufUtils.writeUTF8String(buf, modelName);
        buf.writeByteArray(data);
    }

    @Override
    public void handleOnClient(@Nonnull EntityPlayer player)
    {
        ClientModelLibrary.getInstance().addModelData(modelName, data);
    }

    public static class Handler extends NoReplyHandler<MessageModelData> {}
}
