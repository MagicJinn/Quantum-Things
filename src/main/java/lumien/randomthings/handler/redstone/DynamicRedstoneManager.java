package lumien.randomthings.handler.redstone;

import javax.annotation.Nonnull;
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

import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import lumien.randomthings.capability.redstone.IDynamicRedstone;
import lumien.randomthings.capability.redstone.IDynamicRedstoneManager;
import lumien.randomthings.handler.redstone.signal.ITickableSignal;
import lumien.randomthings.handler.redstone.signal.RedstoneSignal;
import lumien.randomthings.handler.redstone.signal.SignalType;
import lumien.randomthings.util.DimPos;

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

    private final int dimension;
    /* Dynamic redstone levels */
    private final Map<DimPos, EnumMap<EnumFacing, RedstoneSignal>> redstoneLevels;
    /* The state of the redstone levels, strong or weak */
    private final Map<DimPos, Reference2BooleanMap<EnumFacing>> strongPowerStates;
    /* Signals to tick */
    private final Map<DimPos, EnumMap<EnumFacing, ITickableSignal>> tickingSignals;

    public DynamicRedstoneManager(int dimension)
    {
        this.dimension = dimension;
        redstoneLevels = new HashMap<>();
        strongPowerStates = new HashMap<>();
        tickingSignals = new LinkedHashMap<>();
    }

    @Override
    public boolean hasDynamicSignals()
    {
        return !redstoneLevels.isEmpty();
    }

    @Override
    public IDynamicRedstone getDynamicRedstone(DimPos dimPos, @Nonnull EnumFacing side, @Nonnull EnumSet<IDynamicRedstone.Source> allowedSources)
    {
        return new DynamicRedstone(this, dimPos, side, allowedSources);
    }

    public void tick()
    {
        if (tickingSignals.isEmpty()) return;

        Iterator<Map.Entry<DimPos, EnumMap<EnumFacing, ITickableSignal>>> signalsPerSideItr = tickingSignals.entrySet().iterator();
        while (signalsPerSideItr.hasNext())
        {
            Map.Entry<DimPos, EnumMap<EnumFacing, ITickableSignal>> signalsPerSideEntry = signalsPerSideItr.next();
            DimPos pos = signalsPerSideEntry.getKey();
            if (pos.getWorld(false).isBlockLoaded(pos.getPos()))
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
                    signalItr.remove();
                    tickableSignal.onRemoved(this, pos, signalEntry.getKey());
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
    public NBTTagCompound writeNBT(IDynamicRedstoneManager instance, EnumFacing side, NBTTagCompound nbt)
    {
        NBTTagList signalList = new NBTTagList();
        for (Map.Entry<DimPos, EnumMap<EnumFacing, RedstoneSignal>> signalsPerSide : redstoneLevels.entrySet())
        {
            // Don't need the dimension, this manager is dimension specific
            NBTTagCompound pos = NBTUtil.createPosTag(signalsPerSide.getKey().getPos());
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
                signal.writeToNBT(nbt);
                signalList.appendTag(signalData);
            }

        }
        nbt.setTag(SIGNAL_LIST_KEY, signalList);
        return nbt;
    }

    @Override
    public void readNBT(IDynamicRedstoneManager instance, EnumFacing side, NBTTagCompound nbt)
    {
        NBTTagList signalList = nbt.getTagList(SIGNAL_LIST_KEY, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < signalList.tagCount(); i++)
        {
            NBTTagCompound signalData = signalList.getCompoundTagAt(i);

            DimPos pos = DimPos.of(NBTUtil.getPosFromTag(signalData.getCompoundTag(POSITION_KEY)), dimension);
            EnumFacing signalSide = EnumFacing.byIndex(signalData.getByte(SIDE_KEY));

            boolean strongPower = signalData.getBoolean(STRONG_POWER_KEY);

            RedstoneSignal signal = SignalType.readFromNBT(signalData);
            signal.readFromNBT(nbt);

            redstoneLevels.computeIfAbsent(pos, key -> new EnumMap<>(EnumFacing.class))
                    .put(signalSide, signal);
            strongPowerStates.computeIfAbsent(pos, key -> new Reference2BooleanMap<>(EnumFacing.class))
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
        private final DimPos dimPos;
        private final EnumFacing side;
        private final EnumSet<Source> allowedSources;

        public DynamicRedstone(DynamicRedstoneManager manager, DimPos dimPos, EnumFacing side, EnumSet<Source> allowedSources)
        {
            this.manager = manager;
            this.dimPos = dimPos;
            this.side = side;
            this.allowedSources = allowedSources;
        }

        @Override
        public int getRedstoneLevel()
        {
            EnumMap<EnumFacing, RedstoneSignal> signalsPerSide = manager.redstoneLevels.get(dimPos);
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
            EnumMap<EnumFacing, RedstoneSignal> redstoneLevels = manager.redstoneLevels.computeIfAbsent(dimPos, key -> new EnumMap<>(EnumFacing.class));
            Reference2BooleanMap<EnumFacing> strongPowerStates = manager.strongPowerStates.computeIfAbsent(dimPos, key -> new Reference2BooleanMap<>(EnumFacing.class));
            EnumMap<EnumFacing, ITickableSignal> tickingSignals = manager.tickingSignals.computeIfAbsent(dimPos, key -> new EnumMap<>(EnumFacing.class));

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
                manager.redstoneLevels.remove(dimPos);
            }
            if (strongPowerStates.isEmpty())
            {
                manager.strongPowerStates.remove(dimPos);
            }

            if (sendUpdate)
            {
                updateRedstoneInfo(strongPower || sendUpdateStrong);
            }
        }

        public void updateRedstoneInfo(boolean strongPower)
        {
            World world = dimPos.getWorld(false);
            if (world == null)
            {
                return;
            }
            // The position the signal is coming from.
            BlockPos signalPos = dimPos.getPos();
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
            Reference2BooleanMap<EnumFacing> signalsPerSide = manager.strongPowerStates.get(dimPos);
            if (signalsPerSide != null)
            {
                return signalsPerSide.getBoolean(side);
            }
            return false;
        }
    }
}
