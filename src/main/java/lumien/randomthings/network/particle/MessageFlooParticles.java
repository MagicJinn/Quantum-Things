package lumien.randomthings.network.particle;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.google.common.base.Preconditions;
import lumien.randomthings.RandomThings;
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
        int size = buf.readVarInt();
        brickPositions = new ArrayList<>(size);

        for (int i = 0; i < size; i++)
        {
            brickPositions.add(buf.readBlockPos());
        }
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        buf.writeVarInt(brickPositions.size());

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

        RandomThings.proxy.spawnFlooFlameParticles(world, brickPositions);
    }

    public static class Handler extends NoReplyHandler<MessageFlooParticles> {}
}
