package lumien.randomthings.handler.redstone.signal;

import net.minecraft.nbt.NBTTagCompound;

import lumien.randomthings.handler.redstone.source.IDynamicRedstoneSource;
import lumien.randomthings.handler.redstone.source.RedstoneSource;

public class RedstoneSignal
{
    public static final String REDSTONE_LEVEL_KEY = "level";
    public static final String WEAK_LEVEL_KEY = "weakLevel";

    // The overall redstone level; strong prioritized over weak
    protected int redstoneLevel;
    // The weak level, if it exists
    protected int weakLevel;
    protected RedstoneSource source;

    public RedstoneSignal() {}

	public RedstoneSignal(IDynamicRedstoneSource source, int redstoneLevel, boolean strongPower)
	{
        this.source = new RedstoneSource(source);
		this.redstoneLevel = redstoneLevel;
        if (!strongPower)
        {
            weakLevel = redstoneLevel;
        }
	}

    public RedstoneSignal(IDynamicRedstoneSource source, int weakLevel, int strongLevel)
    {
        this(source, strongLevel, true);
        this.weakLevel = weakLevel;
    }

    public int getRedstoneLevel()
    {
        return redstoneLevel;
    }

    public RedstoneSource getSource()
    {
        return source;
    }

    public boolean isStrong()
    {
        return redstoneLevel != weakLevel && redstoneLevel > 0;
    }

	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		compound.setInteger(REDSTONE_LEVEL_KEY, redstoneLevel);
        compound.setInteger(WEAK_LEVEL_KEY, weakLevel);
        source.writeToNBT(compound);
        return compound;
	}

	public void readFromNBT(NBTTagCompound compound)
	{
		redstoneLevel = compound.getInteger(REDSTONE_LEVEL_KEY);
        weakLevel = compound.getInteger(WEAK_LEVEL_KEY);
        source = new RedstoneSource();
        source.readFromNBT(compound);
	}
}
