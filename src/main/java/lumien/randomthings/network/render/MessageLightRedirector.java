package lumien.randomthings.network.render;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import com.google.common.base.Preconditions;
import lumien.randomthings.block.BlockLightRedirector;
import lumien.randomthings.network.ClientboundMessage;

public class MessageLightRedirector implements ClientboundMessage
{
	private int dimension;
	private BlockPos pos;

	public MessageLightRedirector() {}

	public MessageLightRedirector(int dimension, BlockPos pos)
	{
		this.dimension = dimension;
		this.pos = pos;
	}

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        dimension = buf.readVarInt();
        pos = buf.readBlockPos();
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        buf.writeVarInt(dimension);
        buf.writeBlockPos(pos);
    }

    @Override
    public void handleOnClient(@Nonnull EntityPlayer player)
    {
        World world = player.world;
        Preconditions.checkNotNull(world);

        if (world.provider.getDimension() != dimension || !world.isBlockLoaded(pos)) return;

        for (EnumFacing facing : EnumFacing.VALUES)
        {
            BlockPos neighborPos = pos.offset(facing);
            IBlockState neighborState = world.getBlockState(neighborPos);
            if (!(neighborState.getBlock() instanceof BlockLightRedirector)) continue;

            world.notifyBlockUpdate(neighborPos, neighborState, neighborState,
                    Constants.BlockFlags.NOTIFY_NEIGHBORS | Constants.BlockFlags.SEND_TO_CLIENTS);
        }
    }

    public static class Handler extends NoReplyHandler<MessageLightRedirector> {}
}
