package lumien.randomthings.tileentity.redstoneinterface;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import lumien.randomthings.capability.redstone.IDynamicRedstone;
import lumien.randomthings.capability.redstone.IDynamicRedstoneManager;
import lumien.randomthings.handler.redstone.Connection;
import lumien.randomthings.handler.redstone.IRedstoneWriter;
import lumien.randomthings.handler.redstone.signal.RedstoneSignal;
import lumien.randomthings.tileentity.TileEntityBase;
import lumien.randomthings.util.DimPos;

public abstract class TileEntityRedstoneInterface extends TileEntityBase implements ITickable, IRedstoneWriter
{
    private boolean loaded = false;

    protected abstract Set<BlockPos> getTargets();

    @Override
    public void update()
    {
        if (!world.isRemote && !loaded)
        {
            loaded = true;
            for (EnumFacing side : EnumFacing.values())
            {
                sendSignal(side, getTargets());
            }
        }
    }

    @Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block neighborBlock, BlockPos changedPos)
	{
        if (world.isRemote) return;
		BlockPos direction = changedPos.subtract(pos);
        EnumFacing side = EnumFacing.getFacingFromVector(direction.getX(), direction.getY(), direction.getZ());
        sendSignal(side, getTargets());
	}

    /**
     * Sends the signal received by this interface to its targets.
     * @param signalDir The side the received signal is coming from, relative to this tile.
     */
    public void sendSignal(EnumFacing signalDir, Set<BlockPos> targets)
    {
        if (targets.isEmpty()) return;

        BlockPos signalPos = pos.offset(signalDir);
        int strongLevel = world.getStrongPower(signalPos, signalDir);
        int level = world.getRedstonePower(signalPos, signalDir);

        for (BlockPos targetPos : targets)
        {
            if (level <= 0)
            {
                deactivate(targetPos, signalDir);
            }
            else
            {
                setRedstoneLevel(targetPos, signalDir, level, level == strongLevel);
            }
        }
    }

    @Override
    public void onChunkUnload()
    {
        invalidateTargets(getTargets());
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
        invalidateTargets(getTargets());
    }

    protected void invalidateTargets(Set<BlockPos> targets)
    {
        for (EnumFacing side : EnumFacing.values())
        {
            targets.forEach(pos -> deactivate(pos, side));
        }
    }

    @Nonnull
    public Optional<IDynamicRedstone> getDynamicRedstoneFor(BlockPos pos, EnumFacing side)
    {
        IDynamicRedstoneManager managerCap = world.getCapability(IDynamicRedstoneManager.CAPABILITY_DYNAMIC_REDSTONE, null);
        return Optional.ofNullable(managerCap)
                .map(manager -> manager.getDynamicRedstone(DimPos.of(pos, world), side));
    }

    @Override
    public void setRedstoneLevel(BlockPos pos, EnumFacing side, int level, boolean strongPower)
    {
        getDynamicRedstoneFor(pos.offset(side), side).ifPresent(signal -> signal.setRedstoneLevel(new RedstoneSignal(level), strongPower));
    }

    @Override
    public void deactivate(BlockPos pos, EnumFacing side)
    {
        getDynamicRedstoneFor(pos.offset(side), side).ifPresent(signal -> signal.setRedstoneLevel(new RedstoneSignal(IDynamicRedstone.REMOVE_SIGNAL), signal.isStrongSignal()));
    }

    @Override
    public List<Connection> getConnections()
    {
        List<Connection> connections = new ArrayList<>();
        for (BlockPos targetPos : getTargets())
        {
            Connection connection = new Connection(pos, targetPos);
            connections.add(connection);
        }
        return connections;
    }
}
