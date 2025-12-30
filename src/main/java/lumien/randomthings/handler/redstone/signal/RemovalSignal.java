package lumien.randomthings.handler.redstone.signal;

import lumien.randomthings.handler.redstone.source.IDynamicRedstoneSource;
import lumien.randomthings.handler.redstone.source.RedstoneSource;

public class RemovalSignal extends RedstoneSignal
{
    /**
     * Use this redstone level to indicate this signal should be removed from its manager.
     */
    public static final int REMOVE_SIGNAL = -1;

    public RemovalSignal(IDynamicRedstoneSource source, boolean strongPower)
    {
        this.source = new RedstoneSource(source);
        if (strongPower)
        {
            strongLevel = REMOVE_SIGNAL;
        }
        else
        {
            weakLevel = REMOVE_SIGNAL;
        }
    }

    public static boolean isRemovalSignal(RedstoneSignal signal)
    {
        return signal instanceof RemovalSignal &&
                signal.isStrong() ? signal.strongLevel == REMOVE_SIGNAL : signal.weakLevel == REMOVE_SIGNAL;
    }

    @Override
    public int getStrongLevel()
    {
        throw new UnsupportedOperationException("Cannot query level for RemovalSignal!");
    }

    @Override
    public int getWeakLevel()
    {
        throw new UnsupportedOperationException("Cannot query level for RemovalSignal!");
    }

    @Override
    public boolean isStrong()
    {
        return strongLevel == REMOVE_SIGNAL;
    }
}
