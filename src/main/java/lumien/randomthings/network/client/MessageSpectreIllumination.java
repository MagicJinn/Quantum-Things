package lumien.randomthings.network.client;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

import lumien.randomthings.handler.spectreilluminator.SpectreIlluminationClientHandler;
import lumien.randomthings.network.ClientboundMessage;

public class MessageSpectreIllumination implements ClientboundMessage
{
	private int dimension;
	private long chunkLong;

	private boolean illuminated;

	public MessageSpectreIllumination() {}

	public MessageSpectreIllumination(int dimension, long chunkLong, boolean illuminated)
	{
		this.dimension = dimension;
		this.chunkLong = chunkLong;
		this.illuminated = illuminated;
	}

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        dimension = buf.readVarInt();
        chunkLong = buf.readVarLong();
        illuminated = buf.readBoolean();
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        buf.writeVarInt(dimension);
        buf.writeVarLong(chunkLong);
        buf.writeBoolean(illuminated);
    }

    @Override
    public void handleOnClient(@Nonnull EntityPlayer player)
    {
        if (player.world.provider.getDimension() != dimension) return;

        SpectreIlluminationClientHandler.setIlluminated(chunkLong, illuminated);
    }

    public static class Handler extends NoReplyHandler<MessageSpectreIllumination> {}
}
