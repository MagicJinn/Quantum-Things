package lumien.randomthings.handler.redstone.signal;

import java.util.Objects;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import lumien.randomthings.capability.redstone.IDynamicRedstone.Source;
import lumien.randomthings.util.DimPos;

import static lumien.randomthings.capability.redstone.IDynamicRedstone.Source.SOURCE_KEY;

public class RedstoneSignal implements IPositionedSignal
{
    public static final String REDSTONE_LEVEL_KEY = "level";

    protected int redstoneLevel;
    private Source sourceType;
    private DimPos dimPos;
    private EnumFacing side;

    public RedstoneSignal() {}

	public RedstoneSignal(int redstoneLevel, Source sourceType)
	{
		this.redstoneLevel = redstoneLevel;
        this.sourceType = sourceType;
	}

    public int getRedstoneLevel()
    {
        return redstoneLevel;
    }

    public Source getSourceType()
    {
        return sourceType;
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
		compound.setInteger(REDSTONE_LEVEL_KEY, redstoneLevel);
        compound.setByte(SOURCE_KEY, (byte) sourceType.getIndex());
        return compound;
	}

	public void readFromNBT(NBTTagCompound compound)
	{
		redstoneLevel = compound.getInteger(REDSTONE_LEVEL_KEY);
        sourceType = Source.byIndex(compound.getByte(SOURCE_KEY));
	}

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof RedstoneSignal)) return false;
        RedstoneSignal that = (RedstoneSignal) o;
        return redstoneLevel == that.redstoneLevel && sourceType == that.sourceType && Objects.equals(dimPos, that.dimPos) && side == that.side;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(redstoneLevel, sourceType, dimPos, side);
    }
}
