package lumien.randomthings.network.particle;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.google.common.base.Preconditions;
import lumien.randomthings.client.particles.ParticleFlooFlame;
import lumien.randomthings.network.ClientboundMessage;

public class MessageFlooParticles implements ClientboundMessage
{
	private List<BlockPos> brickPositions;

	public MessageFlooParticles() {}

	public MessageFlooParticles(List<BlockPos> brickPositions)
	{
		this.brickPositions = brickPositions;
	}

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        int size = buf.readInt();
        brickPositions = new ArrayList<>(size);

        for (int i = 0; i < size; i++)
        {
            brickPositions.add(buf.readBlockPos());
        }
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        buf.writeInt(brickPositions.size());

        for (BlockPos pos : brickPositions)
        {
            buf.writeBlockPos(pos);
        }
    }

    @Override
    public void handleOnClient(@Nonnull EntityPlayer player)
    {
        World world = player.world;
        Preconditions.checkNotNull(world);

        for (BlockPos pos : brickPositions)
        {
            for (int i = 0; i < 50; i++)
            {
                Particle particle = new ParticleFlooFlame(world,
                        pos.getX() + Math.random(), pos.getY() + 1 + Math.random(), pos.getZ() + Math.random(),
                        0, Math.random() * 0.1, 0);
                Minecraft.getMinecraft().effectRenderer.addEffect(particle);
            }
        }
    }

    public static class Handler extends NoReplyHandler<MessageFlooParticles> {}
}
