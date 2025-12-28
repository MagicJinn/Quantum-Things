package lumien.randomthings.handler.redstone.signal;

import java.util.EnumSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import lumien.randomthings.capability.redstone.IDynamicRedstone;
import lumien.randomthings.capability.redstone.IDynamicRedstoneManager;
import lumien.randomthings.handler.redstone.source.IDynamicRedstoneSource;
import lumien.randomthings.handler.redstone.source.RedstoneSource;

public class TemporarySignal extends RedstoneSignal implements ITickableSignal
{
    public static final String AGE_KEY = "age";
    public static final String DURATION_KEY = "duration";

    private int age;
    private int duration;

    public TemporarySignal()
    {
        super();
    }

    public TemporarySignal(IDynamicRedstoneSource source, int redstoneLevel, boolean strongPower, int duration)
    {
        super(source, redstoneLevel, strongPower);
        this.duration = duration;
    }

    public TemporarySignal(IDynamicRedstoneSource source, int weakLevel, int strongLevel, int duration)
    {
        super(source, weakLevel, strongLevel);
        this.duration = duration;
    }

    @Override
    public int getAge()
    {
        return age;
    }

    @Override
    public int getDuration()
    {
        return duration;
    }

    @Override
    public void tick()
    {
        age++;
    }

    @Override
    public boolean isAlive()
    {
        return age < duration;
    }

    @Override
    public void onRemoved(IDynamicRedstoneManager manager, BlockPos pos, EnumFacing side)
    {
        RedstoneSource source = getSource();
        EnumSet<RedstoneSource.Type> sourceSet = EnumSet.of(source.getType());
        IDynamicRedstone dynamicRedstone = manager.getDynamicRedstone(pos, side, sourceSet);
        // Remove the signal
        dynamicRedstone.setRedstoneLevel(new RedstoneSignal(source, IDynamicRedstone.REMOVE_SIGNAL, dynamicRedstone.isStrongSignal()));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setInteger(DURATION_KEY, duration);
        compound.setInteger(AGE_KEY, age);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        duration = compound.getInteger(DURATION_KEY);
        age = compound.getInteger(AGE_KEY);
    }
}
