package lumien.randomthings.handler.redstone.signal;

import java.util.function.Supplier;

public enum SignalType
{
    CONSTANT(RedstoneSignal::new),
    TEMPORARY(TemporarySignal::new);

    public static final String SIGNAL_TYPE_KEY = "type";

    private final Supplier<RedstoneSignal> signalSupplier;

    SignalType(Supplier<RedstoneSignal> signalSupplier)
    {
        this.signalSupplier = signalSupplier;
    }

    public RedstoneSignal getSignal()
    {
        return signalSupplier.get();
    }
}
