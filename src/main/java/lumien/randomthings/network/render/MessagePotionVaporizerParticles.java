package lumien.randomthings.network.render;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.google.common.base.Preconditions;
import lumien.randomthings.network.ClientboundMessage;

public class MessagePotionVaporizerParticles implements ClientboundMessage
{
	private final List<BlockPos> affectedBlocks;
	private int color;

	public MessagePotionVaporizerParticles(List<BlockPos> affectedBlocks, int color)
	{
		this.affectedBlocks = affectedBlocks;
		this.color = color;
	}

	public MessagePotionVaporizerParticles()
	{
		this.affectedBlocks = new ArrayList<>();
	}

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        color = buf.readInt();
        int numBlocks = buf.readInt();
        for (int i = 0; i < numBlocks; i++)
        {
            affectedBlocks.add(buf.readBlockPos());
        }
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        buf.writeInt(color);
        buf.writeInt(affectedBlocks.size());
        for (BlockPos pos : affectedBlocks)
        {
            buf.writeBlockPos(pos);
        }
    }

    @Override
    public void handleOnClient(@Nonnull EntityPlayer player)
    {
        World world = player.world;
        Preconditions.checkNotNull(world);

        Color c = new Color(color);
        for (BlockPos pos : affectedBlocks)
        {
            world.spawnParticle(EnumParticleTypes.SPELL_MOB,
                    pos.getX() + Math.random(), pos.getY() + Math.random(), pos.getZ() + Math.random(),
                    1D / 255D * c.getRed(), 1D / 255D * c.getGreen(), 1D / 255D * c.getBlue(),
                    0);
        }
    }

    public static class Handler extends NoReplyHandler<MessagePotionVaporizerParticles> {}
}
