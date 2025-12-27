package lumien.randomthings.handler.redstone;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import lumien.randomthings.capability.redstone.IDynamicRedstone;
import lumien.randomthings.capability.redstone.IDynamicRedstoneManager;
import lumien.randomthings.handler.redstone.signal.ITickableSignal;
import lumien.randomthings.handler.redstone.signal.RedstoneSignal;
import lumien.randomthings.handler.redstone.signal.SignalType;

/**
 * Per-world capability that manages dynamic redstone signals.
 * Example sources of such signals are the Redstone Activator, Redstone Remote, and (Advanced) Redstone Interface.
 */
public class DynamicRedstoneManager implements IDynamicRedstoneManager
{
    public static final String SIGNAL_LIST_KEY = "redstoneSignals";
    public static final String POSITION_KEY = "position";
    public static final String SIDE_KEY = "side";
    public static final String STRONG_POWER_KEY = "strongPower";

    private World world;
    /* Dynamic redstone levels */
    private final Map<BlockPos, EnumMap<EnumFacing, RedstoneSignal>> redstoneLevels;
    /* The state of the redstone levels, strong or weak */
    private final Map<BlockPos, Reference2BooleanMap<EnumFacing>> strongPowerStates;
    /* Signals to tick */
    private final Map<BlockPos, EnumMap<EnumFacing, ITickableSignal>> tickingSignals;

    public DynamicRedstoneManager()
    {
        redstoneLevels = new HashMap<>();
        strongPowerStates = new HashMap<>();
        tickingSignals = new LinkedHashMap<>();
    }

    public DynamicRedstoneManager(World world)
    {
        this();
        this.world = world;
    }

    @Nullable
    public World getWorld()
    {
        return world;
    }

    @Override
    public boolean hasDynamicSignals()
    {
        return !redstoneLevels.isEmpty();
    }

    @Override
    public boolean hasTickingSignals()
    {
        return !tickingSignals.isEmpty();
    }

    @Override
    public IDynamicRedstone getDynamicRedstone(BlockPos BlockPos, @Nonnull EnumFacing side, @Nonnull EnumSet<IDynamicRedstone.Source> allowedSources)
    {
        return new DynamicRedstone(this, BlockPos, side, allowedSources);
    }

    public void tick()
    {
        Iterator<Map.Entry<BlockPos, EnumMap<EnumFacing, ITickableSignal>>> signalsPerSideItr = tickingSignals.entrySet().iterator();
        while (signalsPerSideItr.hasNext())
        {
            Map.Entry<BlockPos, EnumMap<EnumFacing, ITickableSignal>> signalsPerSideEntry = signalsPerSideItr.next();
            BlockPos pos = signalsPerSideEntry.getKey();
            World world = getWorld();
            Preconditions.checkNotNull(world, "Tried to tick signals for null world!");

            if (!getWorld().isBlockLoaded(pos))
            {
                continue;
            }
            EnumMap<EnumFacing, ITickableSignal> signalsPerSide = signalsPerSideEntry.getValue();

            Iterator<Map.Entry<EnumFacing, ITickableSignal>> signalItr = signalsPerSide.entrySet().iterator();
            while (signalItr.hasNext())
            {
                Map.Entry<EnumFacing, ITickableSignal> signalEntry = signalItr.next();
                ITickableSignal tickableSignal = signalEntry.getValue();
                tickableSignal.tick();
                if (!tickableSignal.isAlive())
                {
                    tickableSignal.onRemoved(this, pos, signalEntry.getKey());
                    signalItr.remove();
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
        NBTTagList signalList = new NBTTagList();
        for (Map.Entry<BlockPos, EnumMap<EnumFacing, RedstoneSignal>> signalsPerSide : redstoneLevels.entrySet())
        {
            NBTTagCompound pos = NBTUtil.createPosTag(signalsPerSide.getKey());
            for (Map.Entry<EnumFacing, RedstoneSignal> signalEntry : signalsPerSide.getValue().entrySet())
            {
                EnumFacing signalSide = signalEntry.getKey();
                RedstoneSignal signal = signalEntry.getValue();
                NBTTagCompound signalData = new NBTTagCompound();

                signalData.setTag(POSITION_KEY, pos);
                signalData.setByte(SIDE_KEY, (byte) signalSide.getIndex());

                boolean strongPower = false;
                Reference2BooleanMap<EnumFacing> strongSignalsPerSide = strongPowerStates.get(signalsPerSide.getKey());
                if (strongSignalsPerSide != null)
                {
                    strongPower = strongSignalsPerSide.getBoolean(signalSide);
                }
                signalData.setBoolean(STRONG_POWER_KEY, strongPower);

                SignalType.writeToNBT(signalData, signal);
                signal.writeToNBT(signalData);
                signalList.appendTag(signalData);
            }

        }
        rootNBT.setTag(SIGNAL_LIST_KEY, signalList);
        return rootNBT;
    }

    @Override
    public void readNBT(IDynamicRedstoneManager instance, EnumFacing side, NBTTagCompound rootNBT)
    {
        NBTTagList signalList = rootNBT.getTagList(SIGNAL_LIST_KEY, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < signalList.tagCount(); i++)
        {
            NBTTagCompound signalData = signalList.getCompoundTagAt(i);

            BlockPos pos = NBTUtil.getPosFromTag(signalData.getCompoundTag(POSITION_KEY));
            EnumFacing signalSide = EnumFacing.byIndex(signalData.getByte(SIDE_KEY));

            boolean strongPower = signalData.getBoolean(STRONG_POWER_KEY);

            RedstoneSignal signal = SignalType.readFromNBT(signalData);
            signal.readFromNBT(signalData);

            redstoneLevels.computeIfAbsent(pos, key -> new EnumMap<>(EnumFacing.class))
                    .put(signalSide, signal);
            strongPowerStates.computeIfAbsent(pos, key -> new Reference2BooleanOpenHashMap<>())
                    .put(signalSide, strongPower);
            if (signal instanceof ITickableSignal)
            {
                tickingSignals.computeIfAbsent(pos, key -> new EnumMap<>(EnumFacing.class))
                        .put(signalSide, (ITickableSignal) signal);
            }
        }
    }

    public static class DynamicRedstone implements IDynamicRedstone
    {
        private final DynamicRedstoneManager manager;
        private final BlockPos pos;
        private final EnumFacing side;
        private final EnumSet<Source> allowedSources;

        public DynamicRedstone(DynamicRedstoneManager manager, BlockPos pos, EnumFacing side, EnumSet<Source> allowedSources)
        {
            this.manager = manager;
            this.pos = pos;
            this.side = side;
            this.allowedSources = allowedSources;
        }

        @Override
        public int getRedstoneLevel()
        {
            EnumMap<EnumFacing, RedstoneSignal> signalsPerSide = manager.redstoneLevels.get(pos);
            if (signalsPerSide != null)
            {
                RedstoneSignal signal = signalsPerSide.get(side);
                if (signal != null && verifySignalSource(signal))
                {
                    return signal.getRedstoneLevel();
                }
            }
            return -1;
        }

        @Override
        public void setRedstoneLevel(RedstoneSignal signalIn, boolean strongPower)
        {
            if (!verifySignalSource(signalIn))
            {
                throw new IllegalArgumentException("Passed RedstoneSignal's source type cannot be handled by this DynamicRedstone! " +
                        "Expected: " + allowedSources.toString() + ", Actual: " + signalIn.getSourceType());
            }
            int level = signalIn.getRedstoneLevel();
            EnumMap<EnumFacing, RedstoneSignal> redstoneLevels = manager.redstoneLevels.computeIfAbsent(pos, key -> new EnumMap<>(EnumFacing.class));
            Reference2BooleanMap<EnumFacing> strongPowerStates = manager.strongPowerStates.computeIfAbsent(pos, key -> new Reference2BooleanOpenHashMap<>());
            EnumMap<EnumFacing, ITickableSignal> tickingSignals = manager.tickingSignals.computeIfAbsent(pos, key -> new EnumMap<>(EnumFacing.class));

            boolean sendUpdate = false;

            if (redstoneLevels.containsKey(side))
            {
                RedstoneSignal signal = redstoneLevels.get(side);
                // Signal level changed, update
                if (signal.getRedstoneLevel() != level)
                {
                    sendUpdate = true;
                    if (level == REMOVE_SIGNAL)
                    {
                        redstoneLevels.remove(side);
                    }
                    else
                    {
                        redstoneLevels.put(side, signalIn);
                        if (signalIn instanceof ITickableSignal)
                        {
                            tickingSignals.put(side, (ITickableSignal) signalIn);
                        }
                    }
                }
            }
            // Add new signal
            else
            {
                sendUpdate = true;
                if (level != REMOVE_SIGNAL)
                {
                    redstoneLevels.put(side, signalIn);
                    if (signalIn instanceof ITickableSignal)
                    {
                        tickingSignals.put(side, (ITickableSignal) signalIn);
                    }
                }
            }

            boolean sendUpdateStrong = false;
            if (strongPowerStates.containsKey(side))
            {
                // Strong state changed or signal removed, update
                if (strongPowerStates.getBoolean(side) != strongPower || level == REMOVE_SIGNAL)
                {
                    sendUpdateStrong = true;
                    sendUpdate = true;
                    if (level == REMOVE_SIGNAL)
                    {
                        strongPowerStates.removeBoolean(side);
                    }
                    else
                    {
                        strongPowerStates.put(side, strongPower);
                    }
                }
            }
            // Add strong signal state
            else
            {
                sendUpdateStrong = true;
                sendUpdate = true;
                if (level != REMOVE_SIGNAL)
                {
                    strongPowerStates.put(side, strongPower);
                }
            }

            // Cleanup empty maps
            if (redstoneLevels.isEmpty())
            {
                manager.redstoneLevels.remove(pos);
            }
            if (strongPowerStates.isEmpty())
            {
                manager.strongPowerStates.remove(pos);
            }

            if (sendUpdate)
            {
                updateRedstoneInfo(strongPower || sendUpdateStrong);
            }
        }

        public void updateRedstoneInfo(boolean strongPower)
        {
            World world = manager.getWorld();
            Preconditions.checkNotNull(world, "Tried to update dynamic redstone for null world!");

            // The position the signal is coming from.
            BlockPos signalPos = pos;
            // The actual position of the block to emit the signal to.
            BlockPos actualPos = signalPos.offset(side.getOpposite());
            if (world.isBlockLoaded(actualPos))
            {
                // Simulate neighbor change with a block that has (canProvidePower() == true)
                world.neighborChanged(actualPos, Blocks.REDSTONE_BLOCK, signalPos);
                if (strongPower)
                {
                    world.notifyNeighborsOfStateChange(actualPos, Blocks.REDSTONE_BLOCK, false);
                }
            }
        }

        private boolean verifySignalSource(RedstoneSignal signal)
        {
            return allowedSources.contains(signal.getSourceType());
        }

        @Override
        public boolean isStrongSignal()
        {
            Reference2BooleanMap<EnumFacing> signalsPerSide = manager.strongPowerStates.get(pos);
            if (signalsPerSide != null)
            {
                return signalsPerSide.getBoolean(side);
            }
            return false;
        }
    }
}
