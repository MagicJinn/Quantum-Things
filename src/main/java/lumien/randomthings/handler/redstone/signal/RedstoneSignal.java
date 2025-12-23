package lumien.randomthings.handler.redstone.signal;

import net.minecraft.nbt.NBTTagCompound;

import static lumien.randomthings.handler.redstone.signal.SignalType.SIGNAL_TYPE_KEY;

public class RedstoneSignal
{
    public static final String REDSTONE_LEVEL_KEY = "level";

    private int redstoneLevel;

	public RedstoneSignal() {}

	public RedstoneSignal(int redstoneLevel)
	{
		this.redstoneLevel = redstoneLevel;
	}

    public SignalType getSignalType()
    {
        return SignalType.CONSTANT;
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

	public int getRedstoneLevel()
	{
		return redstoneLevel;
	}
}
