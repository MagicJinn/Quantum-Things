package lumien.randomthings.network.particle;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

import com.google.common.base.Preconditions;
import lumien.randomthings.RandomThings;
import lumien.randomthings.network.ClientboundMessage;

public class MessageFlooToken implements ClientboundMessage
{
	private int dimension;
	private double posX;
	private double posY;
	private double posZ;

	public MessageFlooToken() {}

	public MessageFlooToken(int dimension, double posX, double posY, double posZ)
	{
		this.dimension = dimension;
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
	}

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        dimension = buf.readVarInt();
        posX = buf.readDouble();
        posY = buf.readDouble();
        posZ = buf.readDouble();
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        buf.writeVarInt(this.dimension);
        buf.writeDouble(this.posX);
        buf.writeDouble(this.posY);
        buf.writeDouble(this.posZ);
    }

    @Override
    public void handleOnClient(@Nonnull EntityPlayer player)
    {
        World world = player.world;
        Preconditions.checkNotNull(world);

        RandomThings.proxy.spawnFlooTokenParticles(world, dimension, posX, posY, posZ);
    }

    public static class Handler extends NoReplyHandler<MessageFlooToken> {}
}
