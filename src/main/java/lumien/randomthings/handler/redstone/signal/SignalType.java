package lumien.randomthings.handler.redstone.signal;

import java.util.function.Supplier;

import net.minecraft.util.math.MathHelper;

public enum SignalType
{
    CONSTANT(0, RedstoneSignal.class, RedstoneSignal::new),
    TEMPORARY(1, TemporarySignal.class, TemporarySignal::new);

    private final int index;
    private final Class<? extends RedstoneSignal> signalClass;
    private final Supplier<? extends RedstoneSignal> signalSupplier;

    public static final String SIGNAL_TYPE_KEY = "type";
    public static final SignalType[] VALUES = new SignalType[2];

    SignalType(int index, Class<? extends RedstoneSignal> signalClass, Supplier<? extends RedstoneSignal> signalSupplier)
    {
        this.index = index;
        this.signalClass = signalClass;
        this.signalSupplier = signalSupplier;
    }

    public int getIndex()
    {
        return index;
    }

    public RedstoneSignal getSignal()
    {
        return signalSupplier.get();
    }

    public static SignalType byClass(Class<? extends RedstoneSignal> clazz)
    {
        for (SignalType value : VALUES)
        {
            if (value.signalClass == clazz)
            {
                return value;
            }
        }
        throw new IllegalArgumentException("Signal class (" + clazz.getCanonicalName() + ") is of unknown type!");
    }

    public static SignalType byIndex(int index)
    {
        return VALUES[MathHelper.abs(index % VALUES.length)];
    }

    static
    {
        for (SignalType value : values())
        {
            VALUES[value.index] = value;
        }
    }
}
