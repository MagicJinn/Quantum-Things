package lumien.randomthings.handler.redstone.signal;

import net.minecraft.nbt.NBTTagCompound;

import lumien.randomthings.capability.redstone.IDynamicRedstone.Source;

import static lumien.randomthings.capability.redstone.IDynamicRedstone.Source.SOURCE_KEY;

public class RedstoneSignal
{
    public static final String REDSTONE_LEVEL_KEY = "level";

    protected int redstoneLevel;
    protected Source sourceType;

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
}
