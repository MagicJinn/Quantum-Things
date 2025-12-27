package lumien.randomthings.handler.redstone.signal;

import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.Constants;

import lumien.randomthings.capability.redstone.IDynamicRedstone.Source;

import static lumien.randomthings.capability.redstone.IDynamicRedstone.Source.SOURCE_KEY;

public class RedstoneSignal
{
    public static final String REDSTONE_LEVEL_KEY = "level";
    public static final String WEAK_LEVEL_KEY = "weakLevel";

    // The overall redstone level; strong prioritized over weak
    protected int redstoneLevel;
    // The weak level, if it exists
    protected int weakLevel;
    protected Source sourceType;

    public RedstoneSignal() {}

	public RedstoneSignal(int redstoneLevel, Source sourceType, boolean strongPower)
	{
		this.redstoneLevel = redstoneLevel;
        if (!strongPower)
        {
            weakLevel = redstoneLevel;
        }
        this.sourceType = sourceType;
	}

    public RedstoneSignal(int weakLevel, int strongLevel, Source sourceType)
    {
        this(strongLevel, sourceType, true);
        this.weakLevel = weakLevel;
    }

    public int getRedstoneLevel()
    {
        return redstoneLevel;
    }

    public Source getSourceType()
    {
        return sourceType;
    }

    public boolean isStrong()
    {
        return redstoneLevel != weakLevel;
    }

	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		compound.setInteger(REDSTONE_LEVEL_KEY, redstoneLevel);
        if (isStrong())
        {
            compound.setInteger(WEAK_LEVEL_KEY, weakLevel);
        }
        compound.setByte(SOURCE_KEY, (byte) sourceType.getIndex());
        return compound;
	}

	public void readFromNBT(NBTTagCompound compound)
	{
		redstoneLevel = compound.getInteger(REDSTONE_LEVEL_KEY);
        if (compound.hasKey(WEAK_LEVEL_KEY, Constants.NBT.TAG_INT))
        {
            weakLevel = compound.getInteger(WEAK_LEVEL_KEY);
        }
        sourceType = Source.byIndex(compound.getByte(SOURCE_KEY));
	}
}
