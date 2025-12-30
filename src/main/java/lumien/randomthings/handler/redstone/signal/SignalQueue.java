package lumien.randomthings.handler.redstone.signal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.AbstractQueue;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.StreamSupport;

import com.google.common.base.MoreObjects;

/**
 * A queue of redstone signals, sorted by their strength.
 * Equality is determined solely by the signals' source.
 */
public class SignalQueue extends AbstractQueue<RedstoneSignal>
{
    private static final ToIntFunction<RedstoneSignal> SIGNAL_SUM = signal -> signal.strongLevel + signal.weakLevel;

    private final PriorityQueue<RedstoneSignal> signalQueue;
    // Signals mapped by their unique id.
    private final Map<UUID, RedstoneSignal> idToSignals;

    public SignalQueue()
    {
        signalQueue = new PriorityQueue<>(Comparator.comparingInt(SIGNAL_SUM).reversed());
        idToSignals = new HashMap<>();
    }

    public RedstoneSignal getBySource(UUID sourceId)
    {
        return idToSignals.get(sourceId);
    }

    @Nullable
    public RedstoneSignal findFirst(Predicate<RedstoneSignal> predicate)
    {
        RedstoneSignal signal = peek();
        // Check the first signal
        if (predicate.test(signal))
        {
            return signal;
        }
        // Then check the rest of the queue
        if (size() > 1)
        {
            return StreamSupport.stream(spliterator(), false).skip(1).filter(predicate).findFirst().orElse(null);
        }
        return null;
    }

    @Override
    public boolean offer(RedstoneSignal signal)
    {
        UUID id = signal.getSource().getId();
        if (idToSignals.containsKey(id)) {
            RedstoneSignal oldSignal = idToSignals.get(id);
            super.remove(oldSignal);
        }
        signalQueue.offer(signal);
        idToSignals.put(id, signal);
        return true;
    }

    @Override
    public boolean remove(Object o)
    {
        if (!(o instanceof RedstoneSignal))
        {
            return false;
        }
        UUID signalId = ((RedstoneSignal) o).getSource().getId();
        RedstoneSignal toRemove = idToSignals.remove(signalId);
        return signalQueue.remove(toRemove);
    }

    @Override
    public void clear()
    {
        signalQueue.clear();
        idToSignals.clear();
    }

    @Override
    public RedstoneSignal poll()
    {
        RedstoneSignal signal = peek();
        if (signal == null)
        {
            return null;
        }
        UUID signalId = signal.getSource().getId();
        idToSignals.remove(signalId);
        return signalQueue.poll();
    }

    @Override
    public RedstoneSignal peek()
    {
        return signalQueue.peek();
    }

    @Nonnull
    @Override
    public Iterator<RedstoneSignal> iterator()
    {
        return signalQueue.iterator();
    }

    @Override
    public int size()
    {
        return signalQueue.size();
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("queue", signalQueue)
                .add("idToSignals", idToSignals)
                .toString();
    }
}
