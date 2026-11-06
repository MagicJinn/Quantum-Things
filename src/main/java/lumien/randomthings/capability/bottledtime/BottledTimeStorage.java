package lumien.randomthings.capability.bottledtime;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class BottledTimeStorage implements IBottledTime {
    private static final String NBT_BOTTLED_TIME = "bottledTime";

    private long bottledTime;
    // Only used to detect multiple bottles. Not persistent
    private long lastAddedWorldTime = -1;
    // Client-synced AFK pause flag. Not persistent
    private boolean pausedDueToAfk;

    @Override
    public long getBottledTime() {
        return bottledTime;
    }

    @Override
    public void setBottledTime(long bottledTime) {
        this.bottledTime = bottledTime;
    }

    @Override
    public long getLastAddedWorldTime() {
        return lastAddedWorldTime;
    }

    @Override
    public void setLastAddedWorldTime(long worldTime) {
        this.lastAddedWorldTime = worldTime;
    }

    @Override
    public boolean isPausedDueToAfk() {
        return pausedDueToAfk;
    }

    @Override
    public void setPausedDueToAfk(boolean paused) {
        this.pausedDueToAfk = paused;
    }

    @Nonnull
    @Override
    public NBTTagCompound writeNBT(IBottledTime instance, EnumFacing side, NBTTagCompound nbt) {
        nbt.setLong(NBT_BOTTLED_TIME, bottledTime);
        return nbt;
    }

    @Override
    public void readNBT(IBottledTime instance, EnumFacing side, NBTTagCompound nbt) {
        bottledTime = nbt.getLong(NBT_BOTTLED_TIME);
    }
}
