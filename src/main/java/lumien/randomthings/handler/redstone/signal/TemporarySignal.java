package lumien.randomthings.handler.redstone.signal;

import java.util.EnumSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import lumien.randomthings.capability.redstone.IDynamicRedstone;
import lumien.randomthings.capability.redstone.IDynamicRedstoneManager;
import lumien.randomthings.util.DimPos;

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
    public void onRemoved(IDynamicRedstoneManager manager, DimPos pos, EnumFacing side)
    {
        IDynamicRedstone.Source sourceType = getSourceType();
        EnumSet<IDynamicRedstone.Source> sourceSet = EnumSet.of(sourceType);
        IDynamicRedstone dynamicRedstone = manager.getDynamicRedstone(pos, side, sourceSet);
        dynamicRedstone.setRedstoneLevel(new RedstoneSignal(IDynamicRedstone.REMOVE_SIGNAL, sourceType), dynamicRedstone.isStrongSignal());
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
