package lumien.randomthings.handler.redstone;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
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
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
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

    private final int dimension;
    /* Dynamic redstone levels, weak and strong */
    private final Map<DimPos, EnumMap<EnumFacing, RedstoneSignal>> redstoneLevels;
    /* Strong redstone levels */
    private final Map<DimPos, Reference2BooleanMap<EnumFacing>> strongLevels;

    public DynamicRedstoneManager(int dimension)
    {
        this.dimension = dimension;
        redstoneLevels = new HashMap<>();
        strongLevels = new HashMap<>();
    }

    @Override
    public boolean hasDynamicSignals()
    {
        return !redstoneLevels.isEmpty();
    }

    @Override
    public IDynamicRedstone getDynamicRedstone(DimPos dimPos, @Nonnull EnumFacing side)
    {
        return new DynamicRedstone(this, dimPos, side);
    }

    public void tick()
    {
        List<IDynamicRedstone> toDeactivate = new ArrayList<>();
        for (Map.Entry<DimPos, EnumMap<EnumFacing, RedstoneSignal>> signalsPerSideEntry : redstoneLevels.entrySet())
        {
            EnumMap<EnumFacing, RedstoneSignal> signalsPerSide = signalsPerSideEntry.getValue();
            for (Map.Entry<EnumFacing, RedstoneSignal> signalEntry : signalsPerSide.entrySet())
            {
                RedstoneSignal signal = signalEntry.getValue();
                if (signal instanceof ITickableSignal)
                {
                    ITickableSignal tickableSignal = (ITickableSignal) signal;
                    tickableSignal.tick();
                    if (!tickableSignal.isAlive())
                    {
                        toDeactivate.add(getDynamicRedstone(signalsPerSideEntry.getKey(), signalEntry.getKey()));
                    }
                }
            }
        }
        if (toDeactivate.isEmpty()) return;
        for (IDynamicRedstone dynamicRedstone : toDeactivate)
        {
            dynamicRedstone.setRedstoneLevel(new RedstoneSignal(-1), dynamicRedstone.isStrongSignal());
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
                NBTTagCompound signalData = new NBTTagCompound();

                signalData.setTag(POSITION_KEY, pos);
                signalData.setByte(SIDE_KEY, (byte) signalSide.getIndex());
                signalEntry.getValue().writeToNBT(signalData);
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
            SignalType signalType = SignalType.valueOf(signalData.getString(SignalType.SIGNAL_TYPE_KEY));
            RedstoneSignal signal = signalType.getSignal();
            signal.readFromNBT(signalData);

            EnumFacing signalSide = EnumFacing.byIndex(signalData.getByte(SIDE_KEY));
            redstoneLevels.computeIfAbsent(pos, key -> new EnumMap<>(EnumFacing.class))
                    .put(signalSide, signal);
        }
    }

    public static class DynamicRedstone implements IDynamicRedstone
    {
        private final DynamicRedstoneManager manager;
        private final DimPos dimPos;
        private final EnumFacing side;

        public DynamicRedstone(DynamicRedstoneManager manager, DimPos dimPos, EnumFacing side)
        {
            this.manager = manager;
            this.dimPos = dimPos;
            this.side = side;
        }

        @Override
        public int getRedstoneLevel()
        {
            EnumMap<EnumFacing, RedstoneSignal> signalsPerSide = manager.redstoneLevels.get(dimPos);
            if (signalsPerSide != null)
            {
                RedstoneSignal signal = signalsPerSide.get(side);
                return signal != null ? signal.getRedstoneLevel() : REMOVE_SIGNAL;
            }
            return REMOVE_SIGNAL;
        }

        @Override
        public void setRedstoneLevel(RedstoneSignal signalIn, boolean strongPower)
        {
            int level = signalIn.getRedstoneLevel();
            EnumMap<EnumFacing, RedstoneSignal> redstoneLevels = manager.redstoneLevels.computeIfAbsent(dimPos, key -> new EnumMap<>(EnumFacing.class));
            Reference2BooleanMap<EnumFacing> strongSignals = manager.strongLevels.computeIfAbsent(dimPos, key -> new Reference2BooleanOpenHashMap<>());

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
                    }
                }
            }
            // Add new signal
            else
            {
                sendUpdate = true;
                redstoneLevels.put(side, signalIn);
            }

            boolean sendUpdateStrong = false;
            if (strongSignals.containsKey(side))
            {
                // Strong state changed or signal removed, update
                if (strongSignals.getBoolean(side) != strongPower || level == REMOVE_SIGNAL)
                {
                    sendUpdateStrong = true;
                    sendUpdate = true;
                    if (level == REMOVE_SIGNAL)
                    {
                        strongSignals.remove(side);
                    }
                    else
                    {
                        strongSignals.put(side, strongPower);
                    }
                }
            }
            // Add strong signal state
            else
            {
                sendUpdateStrong = true;
                sendUpdate = true;
                strongSignals.put(side, strongPower);
            }

            // Cleanup empty maps
            if (redstoneLevels.isEmpty())
            {
                manager.redstoneLevels.remove(dimPos);
            }
            if (strongSignals.isEmpty())
            {
                manager.strongLevels.remove(dimPos);
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

        @Override
        public boolean isStrongSignal()
        {
            Reference2BooleanMap<EnumFacing> signalsPerSide = manager.strongLevels.get(dimPos);
            if (signalsPerSide != null)
            {
                return signalsPerSide.getBoolean(side);
            }
            return false;
        }
    }
}
