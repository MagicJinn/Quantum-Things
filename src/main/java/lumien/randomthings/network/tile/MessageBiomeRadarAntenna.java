package lumien.randomthings.network.tile;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import com.google.common.base.Preconditions;
import lumien.randomthings.network.ClientboundMessage;
import lumien.randomthings.tileentity.TileEntityBiomeRadar;

public class MessageBiomeRadarAntenna implements ClientboundMessage
{
	private BlockPos pos;
	private final String[] antennaBiomes;

	public MessageBiomeRadarAntenna()
	{
		antennaBiomes = new String[4];
	}

	public MessageBiomeRadarAntenna(String[] antennaBiomes, BlockPos pos)
	{
		this.pos = pos;
		this.antennaBiomes = antennaBiomes;
	}

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        pos = buf.readBlockPos();
        int numBiomes = buf.readInt();
        for (int i = 0; i < numBiomes; i++)
        {
            antennaBiomes[i] = ByteBufUtils.readUTF8String(buf);
        }
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        buf.writeBlockPos(pos);

        int numBiomes = 0;
        for (String antennaBiome : antennaBiomes)
        {
            if (antennaBiome != null)
            {
                numBiomes++;
            }
        }

        buf.writeInt(numBiomes);
        for (String antennaBiome : antennaBiomes)
        {
            if (antennaBiome != null)
            {
                ByteBufUtils.writeUTF8String(buf, antennaBiome);
            }
        }
    }

    @Override
    public void handleOnClient(@Nonnull EntityPlayer player)
    {
        World world = player.world;
        Preconditions.checkNotNull(world);

        if (!world.isBlockLoaded(pos)) return;

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityBiomeRadar)
        {
            ((TileEntityBiomeRadar) tile).setAntennaBiomes(antennaBiomes);
        }
    }

    public static class Handler extends NoReplyHandler<MessageBiomeRadarAntenna> {}
}
