package lumien.randomthings.network.particle;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

import com.google.common.base.Preconditions;
import lumien.randomthings.client.particles.ParticleFlooFlame;
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

        if (world.provider.getDimension() != dimension) return;

        for (double modX = -1; modX <= 1; modX += 0.05)
        {
            for (double modZ = -1; modZ <= 1; modZ += 0.05)
            {
                ParticleFlooFlame particle = new ParticleFlooFlame(world,
                        posX + modX + (Math.random() * 0.1 - 0.05), posY - 1, posZ + modZ + (Math.random() * 0.1 - 0.05),
                        0, Math.random() * 0.3 + 0.1, 0);
                Minecraft.getMinecraft().effectRenderer.addEffect(particle);
            }
        }
    }

    public static class Handler extends NoReplyHandler<MessageFlooToken> {}
}
