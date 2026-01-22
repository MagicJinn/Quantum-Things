package lumien.randomthings.util;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Supplier;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class Lazy<T> implements Supplier<T>
{
    private static final Lazy<Optional<?>> EMPTY = Lazy.of(Optional::empty);

    private final Supplier<T> delegate;
    private T cachedValue;

    private Lazy(Supplier<T> delegate)
    {
        this.delegate = delegate;
    }

    @SuppressWarnings("unchecked")
    public static <T> Lazy<T> empty()
    {
        return (Lazy<T>) EMPTY;
    }

    public static <T> Lazy<T> of(Supplier<T> supplier)
    {
        return new Lazy<>(supplier);
    }

    public static <P extends ICapabilityProvider, T> Lazy<Optional<T>> ofCapability(P provider, Capability<T> capability) {
        return Lazy.of(() -> Optional.ofNullable(provider.getCapability(capability, null)));
    }

    @Nonnull
    public T get() {
        T ret = this.cachedValue;
        if (ret == null) {
            synchronized(this) {
                ret = this.cachedValue;
                if (ret == null) {
                    this.cachedValue = ret = this.delegate.get();
                    if (ret == null) {
                        throw new IllegalStateException("Lazy value cannot be null, but supplier returned null: " + this.delegate);
                    }
                }
            }
        }

        return ret;
    }
}
