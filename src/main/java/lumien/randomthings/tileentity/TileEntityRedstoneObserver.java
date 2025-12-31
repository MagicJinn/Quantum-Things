package lumien.randomthings.tileentity;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;

import com.google.common.base.Preconditions;
import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.capability.redstone.IDynamicRedstone;
import lumien.randomthings.capability.redstone.IDynamicRedstoneManager;
import lumien.randomthings.handler.redstone.Connection;
import lumien.randomthings.handler.redstone.IRedstoneConnectionProvider;
import lumien.randomthings.handler.redstone.component.IRedstoneReader;
import lumien.randomthings.handler.redstone.component.RedstoneWriterDefault;
import lumien.randomthings.handler.redstone.scheduling.ChunkArea;
import lumien.randomthings.handler.redstone.source.IDynamicRedstoneSource;
import lumien.randomthings.handler.redstone.source.RedstoneSource;
import lumien.randomthings.util.Lazy;

import static lumien.randomthings.handler.redstone.source.RedstoneSource.SOURCE_KEY;

public class TileEntityRedstoneObserver extends TileEntityBase implements IDynamicRedstoneSource, IRedstoneConnectionProvider, IRedstoneReader
{
    public static final EnumSet<RedstoneSource.Type> OBSERVED_SOURCE = EnumSet.of(RedstoneSource.Type.OBSERVED);

    @Nonnull
    private Lazy<Optional<IDynamicRedstoneManager>> redstoneManager;
    @Nonnull
    private UUID sourceId;
    private BlockPos target;
    // Internal writer
    private RedstoneWriterDefault writer;

	public TileEntityRedstoneObserver()
	{
        redstoneManager = Lazy.empty();
        sourceId = UUID.randomUUID();
	}

    /**
     * Initialize signals.
     */
    @Override
    public void onLoad()
    {
        if (world.isRemote) return;

        redstoneManager = Lazy.ofCapability(world, IDynamicRedstoneManager.CAPABILITY_DYNAMIC_REDSTONE);
        writer = new RedstoneWriterDefault(redstoneManager, this);
        startObserving(target);
        refreshSignals();
    }

    public static Block getBlock()
    {
        return ModBlocks.redstoneObserver;
    }

    /**
     * Refresh this observer's signals, updating its neighbors when possible.
     */
    public void refreshSignals()
    {
        if (world.isRemote || target == null) return;

        if (!world.isBlockLoaded(target))
        {
            // Schedule task for when targetPos is loaded
            redstoneManager.get().ifPresent(manager ->
                    manager.scheduleTask(ChunkArea.of(target), pos,
                            this::refreshSignals));
            return;
        }

        // Now get the loaded target's state
        IBlockState targetState = world.getBlockState(target);

        for (EnumFacing side : EnumFacing.VALUES)
        {
            int weakLevel = targetState.getWeakPower(world, target, side);
            int strongLevel = targetState.getStrongPower(world, target, side);
            refreshSignalForSide(side, weakLevel, strongLevel);
        }
    }

    /**
     * Side specific variant of {@link #refreshSignals()}.
     * @param side The observer's side to refresh.
     * @param weakLevel The weak power.
     * @param strongLevel The strong power.
     */
    private void refreshSignalForSide(EnumFacing side, int weakLevel, int strongLevel)
    {
        if (!world.isAreaLoaded(pos, 1))
        {
            // Schedule task for when observer neighbors are loaded
            redstoneManager.get().ifPresent(manager ->
                    manager.scheduleTask(ChunkArea.of(pos, 1), pos,
                            () -> this.refreshSignalForSide(side, weakLevel, strongLevel)));
            return;
        }

        if (weakLevel > 0 || strongLevel > 0)
        {
            writer.setRedstoneLevel(getBlock(), pos, side, weakLevel, strongLevel);
        }
        else
        {
            writer.deactivate(getBlock(), pos, side);
        }
        // Update observer's neighbors
        world.notifyNeighborsOfStateChange(pos, getBlock(), false);
    }

    /**
     * Update neighbors for all listening observers.
     * @param event The event for which the observed position was notified.
     */
    public static void notifyNeighbor(BlockEvent.NeighborNotifyEvent event)
    {
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        IDynamicRedstoneManager manager = world.getCapability(IDynamicRedstoneManager.CAPABILITY_DYNAMIC_REDSTONE, null);
        if (manager != null)
        {
            manager.updateObservers(pos, event.getState(), getBlock());
        }
    }

    @Override
    public void onChunkUnload()
    {
        super.onChunkUnload();
        stopObserving(target);
        clearSignals();
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
        stopObserving(target);
        clearSignals();
    }

    /**
     * Clear this observer's signals, updating its neighbors when possible.
     */
    private void clearSignals()
    {
        if (world.isRemote) return;

        if (!world.isAreaLoaded(pos, 1))
        {
            // Schedule task for when observer neighbors are loaded
            redstoneManager.get().ifPresent(manager ->
                    manager.scheduleTask(ChunkArea.of(pos, 1), pos,
                            this::clearSignals));
            return;
        }

        for (EnumFacing side : EnumFacing.VALUES)
        {
            writer.deactivate(getBlock(), pos, side);
        }
        // Update observer's neighbors
        world.notifyNeighborsOfStateChange(pos, getBlock(), false);
    }

    @Override
	public void writeDataToNBT(NBTTagCompound compound, boolean sync)
	{
        super.writeDataToNBT(compound, sync);

		if (target != null)
		{
			compound.setInteger("targetX", target.getX());
			compound.setInteger("targetY", target.getY());
			compound.setInteger("targetZ", target.getZ());
		}
        compound.setUniqueId(SOURCE_KEY, sourceId);
	}

	@Override
	public void readDataFromNBT(NBTTagCompound compound, boolean sync)
	{
        super.readDataFromNBT(compound, sync);

		if (compound.hasKey("targetX"))
		{
			target = new BlockPos(compound.getInteger("targetX"), compound.getInteger("targetY"), compound.getInteger("targetZ"));
		}
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

    /**
     * Start observing a new target, invalidating the old target.
     * @param newTarget The new target's position.
     */
	public void setTarget(BlockPos newTarget)
	{
        if (newTarget.equals(target) || world.isRemote) return;

        BlockPos oldTarget = target;
        target = newTarget;
        stopObserving(oldTarget);
        startObserving(newTarget);

        IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);

        refreshSignals();
    }

    public BlockPos getTarget()
    {
        return target;
    }

    /**
     * Start observing the specified target.
     * @param targetPos The target's position
     */
    protected void startObserving(BlockPos targetPos)
    {
        if (world.isRemote || targetPos == null) return;

        redstoneManager.get().ifPresent(manager -> manager.startObserving(targetPos, this));
    }

    /**
     * Stop observing the specified target.
     * @param targetPos The target's position
     */
    protected void stopObserving(BlockPos targetPos)
    {
        if (world.isRemote || targetPos == null) return;

        redstoneManager.get().ifPresent(manager -> manager.stopObserving(targetPos, this));
    }

    /* Dynamic redstone */

    @Nonnull
    @Override
    public Optional<IDynamicRedstone> getDynamicRedstoneFor(Block block, BlockPos pos, EnumFacing side)
    {
        return redstoneManager.get()
                .map(manager -> manager.getDynamicRedstone(pos.offset(side), side, block, OBSERVED_SOURCE));
    }

    @Override
    public int getRedstoneLevel(Block block, BlockPos pos, EnumFacing side, boolean strongPower)
    {
        return getDynamicRedstoneFor(block, pos, side)
                .map(dynamicRedstone -> dynamicRedstone.getRedstoneLevel(strongPower)).orElse(0);
    }

    @Override
    public RedstoneSource.Type getType()
    {
        return RedstoneSource.Type.OBSERVED;
    }

    @Override
    public UUID getId()
    {
        Preconditions.checkNotNull(sourceId);
        return sourceId;
    }

    /* Block delegate functions */

	public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
	{
        return getRedstoneLevel(blockState.getBlock(), pos, side, false);
	}

	public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
	{
        return getRedstoneLevel(blockState.getBlock(), pos, side, true);
	}

    /* Connection provider */

    @Override
    public List<Connection> getConnections()
    {
        return target == null ? Collections.emptyList() : Collections.singletonList(new Connection(pos, target));
    }
}
