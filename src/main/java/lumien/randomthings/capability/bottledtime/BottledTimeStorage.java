package lumien.randomthings.capability.bottledtime;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class BottledTimeStorage implements IBottledTime {
    private static final String NBT_BOTTLED_TIME = "bottledTime";

    private long bottledTime;
    // Only used to detect multiple bottles. Not persistent
    private long lastAddedWorldTime = -1;

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
