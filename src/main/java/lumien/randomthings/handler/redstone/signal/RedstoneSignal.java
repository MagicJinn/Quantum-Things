package lumien.randomthings.handler.redstone.signal;

import java.util.function.Predicate;

import net.minecraft.nbt.NBTTagCompound;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import lumien.randomthings.handler.redstone.source.IDynamicRedstoneSource;
import lumien.randomthings.handler.redstone.source.RedstoneSource;

public class RedstoneSignal implements Predicate<RedstoneSignal>
{
    public static final String STRONG_LEVEL_KEY = "strongLevel";
    public static final String WEAK_LEVEL_KEY = "weakLevel";

    protected RedstoneSource source;
    // The strong level, if it exists
    protected int strongLevel;
    // The weak level, if it exists
    protected int weakLevel;

    public RedstoneSignal() {}

    /**
     * @param source The signal's source.
     * @param redstoneLevel The signal's redstone level. Must be non-negative.
     * @param strongPower If the level is strong or weak.
     */
	public RedstoneSignal(IDynamicRedstoneSource source, int redstoneLevel, boolean strongPower)
	{
        Preconditions.checkArgument(redstoneLevel >= 0);
        this.source = new RedstoneSource(source);
        if (strongPower)
        {
            strongLevel = redstoneLevel;
        }
        else
        {
            weakLevel = redstoneLevel;
        }
	}

    /**
     * @param source The signal's source.
     * @param weakLevel The signal's weak level. Must be non-negative.
     * @param strongLevel The signal's strong level. Must be non-negative.
     */
    public RedstoneSignal(IDynamicRedstoneSource source, int weakLevel, int strongLevel)
    {
        this(source, strongLevel, true);
        Preconditions.checkArgument(weakLevel >= 0);
        this.weakLevel = weakLevel;
    }

    public int getStrongLevel()
    {
        return strongLevel;
    }

    public int getWeakLevel()
    {
        return weakLevel;
    }

    public RedstoneSource getSource()
    {
        return source;
    }

    public boolean isStrong()
    {
        return strongLevel > 0;
    }

	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		compound.setInteger(STRONG_LEVEL_KEY, strongLevel);
        compound.setInteger(WEAK_LEVEL_KEY, weakLevel);
        source.writeToNBT(compound);
        return compound;
	}

	public void readFromNBT(NBTTagCompound compound)
	{
		strongLevel = compound.getInteger(STRONG_LEVEL_KEY);
        weakLevel = compound.getInteger(WEAK_LEVEL_KEY);
        source = new RedstoneSource();
        source.readFromNBT(compound);
	}

    // Not equals(), because we need reference equality for SignalQueue.
    @Override
    public boolean test(RedstoneSignal signal)
    {
        return strongLevel == signal.strongLevel && weakLevel == signal.weakLevel && getSource().equals(signal.getSource());
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("source", source)
                .add("strongLevel", strongLevel)
                .add("weakLevel", weakLevel)
                .toString();
    }
}
