package lumien.randomthings.network.client;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import lumien.randomthings.network.ClientboundMessage;
import lumien.randomthings.util.WorldUtil;

public class MessageSetBiome implements ClientboundMessage
{
	private BlockPos pos;
	private int biomeId;
	private int dimension;

    public MessageSetBiome() {}

	public MessageSetBiome(BlockPos pos, int biomeId)
	{
		this.pos = pos;
		this.biomeId = biomeId;
	}

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        pos = buf.readBlockPos();
        biomeId = buf.readInt();
        dimension = buf.readInt();
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        buf.writeBlockPos(pos);
        buf.writeInt(biomeId);
        buf.writeInt(dimension);
    }

    @Override
    public void handleOnClient(@Nonnull EntityPlayer player)
    {
        Biome biome = Biome.getBiome(this.biomeId);
        if (biome == null || player.world.provider.getDimension() != dimension) return;

        WorldUtil.setBiome(player.world, pos, biome);
    }

    public static class Handler extends NoReplyHandler<MessageSetBiome> {}
}
