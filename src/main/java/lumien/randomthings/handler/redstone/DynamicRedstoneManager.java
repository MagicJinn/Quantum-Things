package lumien.randomthings.handler.redstone;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import lumien.randomthings.capability.redstone.IDynamicRedstone;
import lumien.randomthings.capability.redstone.IDynamicRedstoneManager;
import lumien.randomthings.handler.redstone.component.IRedstoneWriter;
import lumien.randomthings.handler.redstone.component.RedstoneWriterDefault;
import lumien.randomthings.handler.redstone.scheduling.ChunkArea;
import lumien.randomthings.handler.redstone.scheduling.TaskScheduler;
import lumien.randomthings.handler.redstone.signal.ITickableSignal;
import lumien.randomthings.handler.redstone.signal.RedstoneSignal;
import lumien.randomthings.handler.redstone.signal.RemovalSignal;
import lumien.randomthings.handler.redstone.signal.SignalQueue;
import lumien.randomthings.handler.redstone.signal.TemporarySignal;
import lumien.randomthings.handler.redstone.source.IDynamicRedstoneSource;
import lumien.randomthings.handler.redstone.source.RedstoneSource;
import lumien.randomthings.tileentity.TileEntityRedstoneObserver;

/**
 * Per-world capability that manages dynamic redstone signals.
 * Example sources of such signals are the Redstone Activator, Redstone Remote, and (Advanced) Redstone Interface.
 */
public class DynamicRedstoneManager implements IDynamicRedstoneManager
{
    public static final String TICKABLE_SIGNALS_KEY = "tickableSignals";
    public static final String POSITION_KEY = "position";
    public static final String SIDE_KEY = "side";

    private World world;
    /**
     * Dynamic redstone levels, keyed by position and side.
     * Value is a {@link SignalQueue}, which holds all signals at the same target, sorted by strength.
     * It is not required for the entry's position to currently be loaded.
     */
    private final Map<BlockPos, EnumMap<EnumFacing, SignalQueue>> redstoneLevels;
    /* Signals to tick */
    private final Map<BlockPos, EnumMap<EnumFacing, List<ITickableSignal>>> tickingSignals;
    /* Key: Observed position, Value: Copy of observer source info */
    private final Multimap<BlockPos, RedstoneSource> observers;

    private final TaskScheduler scheduler;

    @SuppressWarnings("UnstableApiUsage")
    public DynamicRedstoneManager()
    {
        redstoneLevels = new HashMap<>();
        tickingSignals = new LinkedHashMap<>();
        observers = MultimapBuilder.hashKeys().hashSetValues().build();
        scheduler = new TaskScheduler();
    }

    public DynamicRedstoneManager(World world)
    {
        this();
        this.world = world;
        scheduler.setWorld(world);
    }

    @Nonnull
    @Override
    public World getWorld()
    {
        Preconditions.checkNotNull(world, "Tried to get a null world from redstone manager!");
        return world;
    }

    @Override
    public boolean hasDynamicSignals()
    {
        return !redstoneLevels.isEmpty();
    }

    @Nonnull
    @Override
    public IDynamicRedstone getDynamicRedstone(BlockPos BlockPos, @Nonnull EnumFacing side, @Nullable Block signalBlock, @Nonnull EnumSet<RedstoneSource.Type> allowedSources)
    {
        return new DynamicRedstone(this, BlockPos, signalBlock, side, allowedSources);
    }

    /* Scheduling */

    @Override
    public void scheduleTask(ChunkArea requiredArea, BlockPos sourcePos, Runnable task)
    {
        scheduler.scheduleTask(requiredArea, sourcePos, task);
    }

    @Override
    public void invalidateTasks(ChunkPos unloadedChunkPos)
    {
        scheduler.invalidateTasks(unloadedChunkPos);
    }

    @Override
    public void runScheduledTasks(ChunkPos loadedChunkPos)
    {
        scheduler.runScheduledTasks(loadedChunkPos);
    }

    /* Observer functions */

    @Override
    public void updateObservers(BlockPos observedPos, IBlockState state, Block observerBlock)
    {
        if (observers.isEmpty() || !observers.containsKey(observedPos)) return;

        for (RedstoneSource observerSource : observers.get(observedPos))
        {
            BlockPos observerPos = observerSource.getSourcePos();
            Preconditions.checkNotNull(observerPos);

            if (!world.isBlockLoaded(observerPos)) continue;

            TileEntity tile = world.getTileEntity(observerPos);
            if (tile instanceof TileEntityRedstoneObserver)
            {
                ((TileEntityRedstoneObserver) tile).refreshSignals();
            }
        }
    }

    @Override
    public void startObserving(BlockPos pos, IDynamicRedstoneSource observer)
    {
        observers.put(pos, new RedstoneSource(observer));
    }

    @Override
    public void stopObserving(BlockPos pos, IDynamicRedstoneSource observer)
    {
        observers.remove(pos, new RedstoneSource(observer));
    }

    /* Ticking */

    @Override
    public boolean hasTickingSignals()
    {
        return !tickingSignals.isEmpty();
    }

    public void tick()
    {
        Iterator<Map.Entry<BlockPos, EnumMap<EnumFacing, List<ITickableSignal>>>> signalsPerSideItr = tickingSignals.entrySet().iterator();
        while (signalsPerSideItr.hasNext())
        {
            Map.Entry<BlockPos, EnumMap<EnumFacing, List<ITickableSignal>>> signalsPerSideEntry = signalsPerSideItr.next();
            BlockPos pos = signalsPerSideEntry.getKey();
            Preconditions.checkNotNull(world, "Tried to tick signals for null world!");

            if (!world.isBlockLoaded(pos))
            {
                continue;
            }
            EnumMap<EnumFacing, List<ITickableSignal>> signalsPerSide = signalsPerSideEntry.getValue();
            Iterator<Map.Entry<EnumFacing, List<ITickableSignal>>> signalListItr = signalsPerSide.entrySet().iterator();
            while (signalListItr.hasNext())
            {
                Map.Entry<EnumFacing, List<ITickableSignal>> signalListEntry = signalListItr.next();
                EnumFacing side = signalListEntry.getKey();
                List<ITickableSignal> signalList = signalListEntry.getValue();

                signalList.removeIf(signal ->
                {
                    signal.tick();
                    if (!signal.isAlive())
                    {
                        signal.onRemoved(this, pos, side);
                        return true;
                    }
                    return false;
                });

                if (signalList.isEmpty())
                {
                    signalListItr.remove();
                }
            }

            if (signalsPerSide.isEmpty())
            {
                signalsPerSideItr.remove();
            }
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeNBT(IDynamicRedstoneManager instance, EnumFacing side, NBTTagCompound rootNBT)
    {
        // Only tickable signals need to be saved, other ones will be recreated by their source.
        NBTTagList tickableSignals = new NBTTagList();
        for (Map.Entry<BlockPos, EnumMap<EnumFacing, List<ITickableSignal>>> signalsPerSide : tickingSignals.entrySet())
        {
            NBTTagCompound pos = NBTUtil.createPosTag(signalsPerSide.getKey());
            for (Map.Entry<EnumFacing, List<ITickableSignal>> signalQueueEntry : signalsPerSide.getValue().entrySet())
            {
                EnumFacing signalSide = signalQueueEntry.getKey();
                for (ITickableSignal signal : signalQueueEntry.getValue())
                {
                    NBTTagCompound signalData = new NBTTagCompound();

                    signalData.setTag(POSITION_KEY, pos);
                    signalData.setByte(SIDE_KEY, (byte) signalSide.getIndex());

                    signal.writeToNBT(signalData);
                    tickableSignals.appendTag(signalData);
                }
            }

        }
        rootNBT.setTag(TICKABLE_SIGNALS_KEY, tickableSignals);
        return rootNBT;
    }

    @Override
    public void readNBT(IDynamicRedstoneManager instance, EnumFacing side, NBTTagCompound rootNBT)
    {
        // Read tickable signals
        NBTTagList signalList = rootNBT.getTagList(TICKABLE_SIGNALS_KEY, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < signalList.tagCount(); i++)
        {
            NBTTagCompound signalData = signalList.getCompoundTagAt(i);

            BlockPos pos = NBTUtil.getPosFromTag(signalData.getCompoundTag(POSITION_KEY));
            EnumFacing signalSide = EnumFacing.byIndex(signalData.getByte(SIDE_KEY));

            TemporarySignal signal = new TemporarySignal();
            signal.readFromNBT(signalData);

            // Add tickable signal to both maps
            redstoneLevels.computeIfAbsent(pos, key -> new EnumMap<>(EnumFacing.class))
                    .computeIfAbsent(signalSide, key -> new SignalQueue())
                    .offer(signal);
            tickingSignals.computeIfAbsent(pos, key -> new EnumMap<>(EnumFacing.class))
                    .computeIfAbsent(signalSide, key -> new ArrayList<>())
                    .add(signal);
        }
    }

    public static class DynamicRedstone implements IDynamicRedstone
    {
        private final DynamicRedstoneManager manager;
        private final BlockPos pos;
        private final Block block;
        private final EnumFacing side;
        private final EnumSet<RedstoneSource.Type> allowedSources;

        public DynamicRedstone(DynamicRedstoneManager manager, BlockPos pos, @Nullable Block block, EnumFacing side, EnumSet<RedstoneSource.Type> allowedSources)
        {
            this.manager = manager;
            this.pos = pos;
            this.block = block != null ? block : Blocks.REDSTONE_BLOCK;
            this.side = side;
            this.allowedSources = allowedSources;
        }

        @Override
        public int getRedstoneLevel(boolean strongPower)
        {
            EnumMap<EnumFacing, SignalQueue> signalsPerSide = manager.redstoneLevels.get(pos);
            if (signalsPerSide != null)
            {
                SignalQueue signalQueue = signalsPerSide.get(side);
                if (signalQueue != null && !signalQueue.isEmpty())
                {
                    RedstoneSignal signal = signalQueue.findFirst(this::canHandleSignal);
                    if (signal != null)
                    {
                        return strongPower ? signal.getStrongLevel() : signal.getWeakLevel();
                    }
                }
            }
            return -1;
        }

        @Override
        public void setRedstoneLevel(RedstoneSignal signalIn)
        {
            if (!canHandleSignal(signalIn))
            {
                throw new IllegalArgumentException("Passed RedstoneSignal's source type cannot be handled by this DynamicRedstone! " +
                        "Expected: " + allowedSources.toString() + ", Actual: " + signalIn.getSource().getType());
            }
            EnumMap<EnumFacing, SignalQueue> redstoneLevels = manager.redstoneLevels.computeIfAbsent(pos, key -> new EnumMap<>(EnumFacing.class));
            EnumMap<EnumFacing, List<ITickableSignal>> tickingSignals = manager.tickingSignals.computeIfAbsent(pos, key -> new EnumMap<>(EnumFacing.class));

            boolean sendUpdate = false;
            boolean sendUpdateStrong = false;

            SignalQueue signalQueue = redstoneLevels.get(side);
            RedstoneSignal signal = signalQueue != null ? signalQueue.getBySource(signalIn.getSource().getId()) : null;
            if (signal != null)
            {
                boolean signalLevelChanged = !signal.test(signalIn);
                boolean strongStateChanged = signal.isStrong() != signalIn.isStrong();
                // Signal level or strength state changed, update
                if (signalLevelChanged || strongStateChanged)
                {
                    sendUpdate = true;
                    sendUpdateStrong = strongStateChanged;
                    if (RemovalSignal.isRemovalSignal(signalIn))
                    {
                        signalQueue.remove(signalIn);
                    }
                    else
                    {
                        redstoneLevels.computeIfAbsent(side, key -> new SignalQueue()).offer(signalIn);
                        if (signalIn instanceof ITickableSignal)
                        {
                            tickingSignals.computeIfAbsent(side, key -> new ArrayList<>()).add((ITickableSignal) signalIn);
                        }
                    }
                }
            }
            // Add new signal
            else if (!RemovalSignal.isRemovalSignal(signalIn))
            {
                sendUpdate = true;
                sendUpdateStrong = signalIn.isStrong();
                signalQueue = redstoneLevels.computeIfAbsent(side, key -> new SignalQueue());
                signalQueue.offer(signalIn);
                if (signalIn instanceof ITickableSignal)
                {
                    tickingSignals.computeIfAbsent(side, key -> new ArrayList<>()).add((ITickableSignal) signalIn);
                }
            }

            // Cleanup empty maps
            if (signalQueue != null && signalQueue.isEmpty())
            {
                redstoneLevels.remove(side);
            }
            if (redstoneLevels.isEmpty())
            {
                manager.redstoneLevels.remove(pos);
            }
            if (tickingSignals.isEmpty())
            {
                manager.tickingSignals.remove(pos);
            }

            if (sendUpdate)
            {
                updateRedstoneInfo(signalIn.isStrong() || sendUpdateStrong);
            }
        }

        public void updateRedstoneInfo(boolean strongPower)
        {
            World world = manager.getWorld();

            // The position the signal is coming from.
            BlockPos signalPos = pos;
            // The actual position of the block to emit the signal to.
            BlockPos actualPos = signalPos.offset(side.getOpposite());
            if (world.isBlockLoaded(actualPos))
            {
                world.neighborChanged(actualPos, block, signalPos);
                if (strongPower)
                {
                    world.notifyNeighborsOfStateChange(actualPos, block, false);
                }
            }
        }

        private boolean canHandleSignal(RedstoneSignal signal)
        {
            return allowedSources.contains(signal.getSource().getType());
        }

        @Override
        public boolean isStrongSignal()
        {
            EnumMap<EnumFacing, SignalQueue> signalsPerSide = manager.redstoneLevels.get(pos);
            if (signalsPerSide != null)
            {
                SignalQueue signalQueue = signalsPerSide.get(side);
                if (signalQueue != null && !signalQueue.isEmpty())
                {
                    RedstoneSignal signal = signalQueue.findFirst(this::canHandleSignal);
                    if (signal != null)
                    {
                        return signal.isStrong();
                    }
                }
            }
            return false;
        }
    }
}
