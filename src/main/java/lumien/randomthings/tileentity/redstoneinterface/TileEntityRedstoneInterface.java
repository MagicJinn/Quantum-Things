package lumien.randomthings.tileentity.redstoneinterface;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import lumien.randomthings.RandomThings;
import lumien.randomthings.capability.redstone.IDynamicRedstone;
import lumien.randomthings.capability.redstone.IDynamicRedstoneManager;
import lumien.randomthings.handler.redstone.Connection;
import lumien.randomthings.handler.redstone.IRedstoneConnectionProvider;
import lumien.randomthings.handler.redstone.component.IRedstoneWriter;
import lumien.randomthings.handler.redstone.signal.RedstoneSignal;
import lumien.randomthings.handler.redstone.signal.RemovalSignal;
import lumien.randomthings.handler.redstone.source.IDynamicRedstoneSource;
import lumien.randomthings.handler.redstone.source.RedstoneSource;
import lumien.randomthings.tileentity.TileEntityBase;
import lumien.randomthings.util.Lazy;
import lumien.randomthings.util.WorldUtil;

import static lumien.randomthings.handler.redstone.source.RedstoneSource.SOURCE_KEY;

public abstract class TileEntityRedstoneInterface extends TileEntityBase implements IDynamicRedstoneSource, IRedstoneConnectionProvider, IRedstoneWriter
{
    public static final EnumSet<RedstoneSource.Type> INTERFACE_SOURCE = EnumSet.of(RedstoneSource.Type.INTERFACE);

    @Nonnull
    private Lazy<Optional<IDynamicRedstoneManager>> redstoneManager;
    @Nonnull
    protected UUID sourceId;

    public TileEntityRedstoneInterface()
    {
        redstoneManager = Lazy.empty();
        sourceId = UUID.randomUUID();
    }

    protected abstract Set<BlockPos> getTargets();

    @Override
    public void onLoad()
    {
        if (world.isRemote) return;

        redstoneManager = Lazy.ofCapability(world, IDynamicRedstoneManager.CAPABILITY_DYNAMIC_REDSTONE);
        sendSignalsToAll(getTargets());
    }

    @Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block neighborBlock, BlockPos changedPos)
	{
        if (world.isRemote) return;

		BlockPos direction = changedPos.subtract(pos);
        EnumFacing side = EnumFacing.getFacingFromVector(direction.getX(), direction.getY(), direction.getZ());
        sendSidedSignal(side, neighborBlock, getTargets());
	}

    /**
     * Sends the signal received by this interface to its targets on the specified side.
     * @param signalDir The side the received signal is coming from, relative to this tile.
     * @param signalBlock The block sending the signal.
     * @param targets The targets' positions.
     */
    private void sendSidedSignal(EnumFacing signalDir, Block signalBlock, Set<BlockPos> targets)
    {
        if (targets.isEmpty()) return;

        SignalInfo signalInfo = new SignalInfo();
        int weakLevel = signalInfo.weakLevels.get(signalDir);
        int strongLevel = signalInfo.strongLevels.get(signalDir);

        for (BlockPos targetPos : targets)
        {
            if (!world.isBlockLoaded(targetPos))
            {
                // Schedule all-sided task for when targetPos is loaded
                redstoneManager.get().ifPresent(manager ->
                        manager.scheduleTask(targetPos, pos,
                                () -> this.sendSignalsTo(targetPos, signalInfo)));
                continue;
            }

            Block targetBlock = world.getBlockState(targetPos).getBlock();
            if (weakLevel > 0 || strongLevel > 0)
            {
                setRedstoneLevel(signalBlock, targetPos, signalDir, weakLevel, strongLevel);
            }
            else
            {
                deactivate(signalBlock, targetPos, signalDir);
            }
            // Update target's neighbors
            if (WorldUtil.isNeighboring(targetPos, pos))
            {
                // Don't recursively update signal -> interface -> signal -> ...
                world.notifyNeighborsOfStateExcept(targetPos, targetBlock, signalDir.getOpposite());
            }
            else
            {
                world.notifyNeighborsOfStateChange(targetPos, targetBlock, false);
            }
        }
    }

    /**
     * Send all signals to the target, waiting for the target to be loaded.
     * @param targetPos The target position.
     */
    private void sendSignalsTo(BlockPos targetPos, SignalInfo signals)
    {
        if (!world.isBlockLoaded(targetPos))
        {
            // Schedule self for when targetPos is loaded
            redstoneManager.get().ifPresent(manager ->
                    manager.scheduleTask(targetPos, pos,
                            () -> this.sendSignalsTo(targetPos, signals)));
            return;
        }

        // Now get the loaded target's state
        Block targetBlock = world.getBlockState(targetPos).getBlock();
        for (EnumFacing side : EnumFacing.VALUES)
        {
            int weakLevel = signals.weakLevels.get(side);
            int strongLevel = signals.strongLevels.get(side);
            Block signalBlock = signals.signalBlocks.get(side);

            if (weakLevel > 0 || strongLevel > 0)
            {
                setRedstoneLevel(signalBlock, targetPos, side, weakLevel, strongLevel);
            }
            else
            {
                deactivate(signalBlock, targetPos, side);
            }
        }
        // Update target's neighbors
        world.notifyNeighborsOfStateChange(targetPos, targetBlock, false);
    }

    /**
     * Sends all signals received by this interface to all specified targets.
     * @param targets The targets' positions.
     */
    public void sendSignalsToAll(Set<BlockPos> targets)
    {
        if (world.isRemote || targets.isEmpty()) return;

        SignalInfo signalInfo = new SignalInfo();
        for (BlockPos target : targets)
        {
            sendSignalsTo(target, signalInfo);
        }
    }

    @Override
    public void onChunkUnload()
    {
        super.onChunkUnload();
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
        if (world.isRemote || targets.isEmpty()) return;

        // Blocks.AIR to always deactivate
        SignalInfo signalInfo = new SignalInfo(Blocks.AIR);
        for (BlockPos target : targets)
        {
            sendSignalsTo(target, signalInfo);
        }
    }

    @Override
    public void writeDataToNBT(NBTTagCompound compound, boolean sync)
    {
        super.writeDataToNBT(compound, sync);

        compound.setUniqueId(SOURCE_KEY, sourceId);
    }

    @Override
    public void readDataFromNBT(NBTTagCompound compound, boolean sync)
    {
        super.readDataFromNBT(compound, sync);

        if (compound.hasUniqueId(SOURCE_KEY))
        {
            UUID sourceId = compound.getUniqueId(SOURCE_KEY);
            Preconditions.checkNotNull(sourceId);
            this.sourceId = sourceId;
        }
        else
        {
            sourceId = UUID.randomUUID();
        }
    }

    /* Dynamic redstone */

    @Nonnull
    @Override
    public Optional<IDynamicRedstone> getDynamicRedstoneFor(Block block, BlockPos pos, EnumFacing side)
    {
        return redstoneManager.get()
                .map(manager -> manager.getDynamicRedstone(pos.offset(side), side, block, INTERFACE_SOURCE));
    }

    @Override
    public void setRedstoneLevel(Block block, BlockPos pos, EnumFacing side, int weakLevel, int strongLevel)
    {
        getDynamicRedstoneFor(block, pos, side).ifPresent(dynamicRedstone ->
                dynamicRedstone.setRedstoneLevel(new RedstoneSignal(TileEntityRedstoneInterface.this, weakLevel, strongLevel)));
    }

    @Override
    public void deactivate(Block block, BlockPos pos, EnumFacing side)
    {
        getDynamicRedstoneFor(block, pos, side).ifPresent(dynamicRedstone ->
                dynamicRedstone.setRedstoneLevel(new RemovalSignal(TileEntityRedstoneInterface.this, dynamicRedstone.isStrongSignal())));
    }

    @Override
    public RedstoneSource.Type getType()
    {
        return RedstoneSource.Type.INTERFACE;
    }

    @Override
    public UUID getId()
    {
        Preconditions.checkNotNull(sourceId);
        return sourceId;
    }

    @Nullable
    @Override
    public BlockPos getSourcePos()
    {
        return pos;
    }

    /* Connection provider */

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

    /**
     * Signal info for all neighbors of this interface.
     */
    private class SignalInfo
    {
        final EnumMap<EnumFacing, Integer> weakLevels;
        final EnumMap<EnumFacing, Integer> strongLevels;
        final EnumMap<EnumFacing, Block> signalBlocks;

        SignalInfo()
        {
            this(null);
        }

        SignalInfo(@Nullable Block blockOverride)
        {
            weakLevels = new EnumMap<>(EnumFacing.class);
            strongLevels = new EnumMap<>(EnumFacing.class);
            signalBlocks = new EnumMap<>(EnumFacing.class);

            // Gather signal info
            for (EnumFacing side : EnumFacing.VALUES)
            {
                BlockPos signalPos = pos.offset(side);
                int weakLevel = world.getRedstonePower(signalPos, side);
                int strongLevel = world.getStrongPower(signalPos, side);
                Block signalBlock = blockOverride != null ? blockOverride : world.getBlockState(signalPos).getBlock();

                weakLevels.put(side, weakLevel);
                strongLevels.put(side, strongLevel);
                signalBlocks.put(side, signalBlock);
            }
        }

        @Override
        public String toString()
        {
            return MoreObjects.toStringHelper(this)
                    .add("weakLevels", weakLevels)
                    .add("strongLevels", strongLevels)
                    .add("signalBlocks", signalBlocks)
                    .toString();
        }
    }
}
