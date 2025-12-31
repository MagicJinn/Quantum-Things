package lumien.randomthings.tileentity.redstoneinterface;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
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

import com.google.common.base.Preconditions;
import lumien.randomthings.capability.redstone.IDynamicRedstone;
import lumien.randomthings.capability.redstone.IDynamicRedstoneManager;
import lumien.randomthings.handler.redstone.Connection;
import lumien.randomthings.handler.redstone.IRedstoneConnectionProvider;
import lumien.randomthings.handler.redstone.component.IRedstoneWriter;
import lumien.randomthings.handler.redstone.scheduling.ChunkArea;
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
        sendSignal(getTargets());
    }

    @Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block neighborBlock, BlockPos changedPos)
	{
        if (world.isRemote) return;

		BlockPos direction = changedPos.subtract(pos);
        EnumFacing side = EnumFacing.getFacingFromVector(direction.getX(), direction.getY(), direction.getZ());
        sendSidedSignal(changedPos, side, neighborBlock, getTargets(), false);
	}

    /**
     * Sends the signal received by this interface to its targets on the specified side.
     * @param signalPos The signal's position - must be loaded.
     * @param signalDir The side the received signal is coming from, relative to this tile.
     * @param signalBlockIn The block sending the signal, null if not loaded yet.
     * @param targets The targets' positions.
     * @param checkPower If the power in-world should be checked.
     */
    private void sendSidedSignal(BlockPos signalPos, EnumFacing signalDir, @Nullable Block signalBlockIn, Set<BlockPos> targets, boolean checkPower)
    {
        if (targets.isEmpty()) return;

        if (!world.isAreaLoaded(signalPos, 1))
        {
            // Schedule task for when signalPos is loaded
            redstoneManager.get().ifPresent(manager ->
                    manager.scheduleTask(ChunkArea.of(signalPos, 1), pos,
                            () -> this.sendSidedSignal(signalPos, signalDir, signalBlockIn, targets, checkPower)));
            return;
        }

        Block signalBlock = signalBlockIn != null ? signalBlockIn : world.getBlockState(signalPos).getBlock();
        // Requires neighbors of signalPos to be loaded
        int weakLevel = checkPower ? 0 : world.getRedstonePower(signalPos, signalDir);
        int strongLevel = checkPower ? 0 : world.getStrongPower(signalPos, signalDir);

        for (BlockPos targetPos : targets)
        {
            sendSidedSignalToTarget(signalDir, signalBlock, targetPos, weakLevel, strongLevel);
        }
    }

    /**
     * Single target variant of {@link #sendSidedSignal(BlockPos, EnumFacing, Block, Set, boolean)}.
     * @param signalDir The side the received signal is coming from, relative to this tile.
     * @param signalBlock The signal's block.
     * @param targetPos The targets' position - must be loaded.
     * @param weakLevel The signal's weak power.
     * @param strongLevel The signal's strong power.
     */
    private void sendSidedSignalToTarget(EnumFacing signalDir, @Nonnull Block signalBlock,
                                         BlockPos targetPos, int weakLevel, int strongLevel)
    {
        if (!world.isAreaLoaded(targetPos, 1))
        {
            // Schedule task for when targetPos is loaded
            redstoneManager.get().ifPresent(manager ->
                    manager.scheduleTask(ChunkArea.of(targetPos, 1), pos,
                            () -> this.sendSidedSignalToTarget(signalDir, signalBlock, targetPos, weakLevel, strongLevel)));
            return;
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

    /**
     * Sends the signal received by this interface to its targets for all sides.
     * @param targets The targets' positions.
     */
    public void sendSignal(Set<BlockPos> targets)
    {
        if (targets.isEmpty()) return;

        for (EnumFacing side : EnumFacing.VALUES)
        {
            BlockPos signalPos = pos.offset(side);
            sendSidedSignal(signalPos, side, null, targets, false);
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

        for (EnumFacing side : EnumFacing.VALUES)
        {
            BlockPos signalPos = pos.offset(side);
            sendSidedSignal(signalPos, side, Blocks.AIR, targets, true);
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
}
