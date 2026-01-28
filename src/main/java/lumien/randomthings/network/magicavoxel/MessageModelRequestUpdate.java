package lumien.randomthings.network.magicavoxel;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import lumien.randomthings.handler.magicavoxel.ClientModelLibrary;
import lumien.randomthings.network.ClientboundMessage;

public class MessageModelRequestUpdate implements ClientboundMessage
{
	private String modelName;
	private int modelSize;
	private int paletteSize;

    public MessageModelRequestUpdate() {}

	public void setData(String modelName, int modelSize, int paletteSize)
	{
		this.modelName = modelName;
		this.modelSize = modelSize;
		this.paletteSize = paletteSize;
	}

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        modelName = ByteBufUtils.readUTF8String(buf);
        modelSize = buf.readVarInt();
        paletteSize = buf.readVarInt();
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        ByteBufUtils.writeUTF8String(buf, this.modelName);
        buf.writeVarInt(this.modelSize);
        buf.writeVarInt(this.paletteSize);
    }

    @Override
    public void handleOnClient(@Nonnull EntityPlayer player)
    {
        ClientModelLibrary.getInstance().updateRequest(modelName, modelSize, paletteSize);
    }

    public static class Handler extends NoReplyHandler<MessageModelRequestUpdate> {}
}
