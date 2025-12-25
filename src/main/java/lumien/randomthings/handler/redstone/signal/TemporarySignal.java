package lumien.randomthings.handler.redstone.signal;

import java.util.Objects;

import net.minecraft.nbt.NBTTagCompound;

import lumien.randomthings.capability.redstone.IDynamicRedstone;

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

    public TemporarySignal(int redstoneLevel, int duration, IDynamicRedstone.Source sourceType)
    {
        super(redstoneLevel, sourceType);
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

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof TemporarySignal)) return false;
        if (!super.equals(o)) return false;
        TemporarySignal that = (TemporarySignal) o;
        return duration == that.duration;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), duration);
    }
}
