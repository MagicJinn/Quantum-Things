package lumien.randomthings.handler.redstone.signal;

import java.util.Objects;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import lumien.randomthings.util.DimPos;

import static lumien.randomthings.handler.redstone.signal.SignalType.SIGNAL_TYPE_KEY;

public class RedstoneSignal implements IPositionedSignal
{
    public static final String REDSTONE_LEVEL_KEY = "level";

    protected int redstoneLevel;
    private DimPos dimPos;
    private EnumFacing side;

	public RedstoneSignal() {}

	public RedstoneSignal(int redstoneLevel)
	{
		this.redstoneLevel = redstoneLevel;
	}

    public SignalType getSignalType()
    {
        return SignalType.CONSTANT;
    }

    public int getRedstoneLevel()
    {
        return redstoneLevel;
    }

    @Override
    public DimPos getPos()
    {
        return dimPos;
    }

    @Override
    public EnumFacing getSide()
    {
        return side;
    }

    public void setContext(DimPos dimPos, EnumFacing side)
    {
        this.dimPos = dimPos;
        this.side = side;
    }

	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
        compound.setString(SIGNAL_TYPE_KEY, getSignalType().name());
		compound.setInteger(REDSTONE_LEVEL_KEY, redstoneLevel);
        return compound;
	}

	public void readFromNBT(NBTTagCompound compound)
	{
		redstoneLevel = compound.getInteger(REDSTONE_LEVEL_KEY);
	}

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof RedstoneSignal)) return false;
        RedstoneSignal that = (RedstoneSignal) o;
        return redstoneLevel == that.redstoneLevel;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(redstoneLevel);
    }
}
