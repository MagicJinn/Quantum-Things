package lumien.randomthings.handler.redstone.signal;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * A queue of redstone signals, sorted by their strength.
 * Equality is determined solely by the signals' source.
 */
public class SignalQueue
{
    // Multiple signals are pretty rare, so use a small initial capacity.
    private static final int INITIAL_CAPACITY = 4;
    private static final Comparator<RedstoneSignal> STRONG_COMPARE = Comparator.comparingInt(RedstoneSignal::getStrongLevel).reversed();
    private static final Comparator<RedstoneSignal> WEAK_COMPARE = Comparator.comparingInt(RedstoneSignal::getWeakLevel).reversed();

    // Both queues contain the same set of signals, just sorted differently.
    private final PriorityQueue<RedstoneSignal> strongQueue;
    private final PriorityQueue<RedstoneSignal> weakQueue;
    // Signals mapped by their unique id.
    private final Map<UUID, RedstoneSignal> idToSignals;

    public SignalQueue()
    {
        strongQueue = new PriorityQueue<>(INITIAL_CAPACITY, STRONG_COMPARE);
        weakQueue = new PriorityQueue<>(INITIAL_CAPACITY, WEAK_COMPARE);
        idToSignals = new HashMap<>(INITIAL_CAPACITY);
    }

    public RedstoneSignal getBySource(UUID sourceId)
    {
        return idToSignals.get(sourceId);
    }

    @Nullable
    public RedstoneSignal findFirst(Predicate<RedstoneSignal> predicate, boolean strongPower)
    {
        RedstoneSignal signal = strongPower ? strongQueue.peek() : weakQueue.peek();
        // Check the first signal
        if (predicate.test(signal))
        {
            return signal;
        }
        // Then check the rest of the queue
        if (size() > 1)
        {
            return stream(strongPower).skip(1).filter(predicate).findFirst().orElse(null);
        }
        return null;
    }

    private Stream<RedstoneSignal> stream(boolean strongPower)
    {
        RedstoneSignal[] signals = (strongPower ? strongQueue : weakQueue).toArray(new RedstoneSignal[0]);
        return Arrays.stream(signals).sorted(strongPower ? STRONG_COMPARE : WEAK_COMPARE);
    }

    public boolean add(RedstoneSignal signal)
    {
        UUID id = signal.getSource().getId();
        if (idToSignals.containsKey(id)) {
            RedstoneSignal oldSignal = idToSignals.get(id);
            remove(oldSignal);
        }
        strongQueue.offer(signal);
        weakQueue.offer(signal);
        idToSignals.put(id, signal);
        checkSize();
        return true;
    }

    public boolean remove(Object o)
    {
        if (!(o instanceof RedstoneSignal))
        {
            return false;
        }
        UUID signalId = ((RedstoneSignal) o).getSource().getId();
        RedstoneSignal toRemove = idToSignals.remove(signalId);
        boolean removed = strongQueue.remove(toRemove);
        weakQueue.remove(toRemove);
        checkSize();
        return removed;
    }

    private void checkSize()
    {
        Preconditions.checkState(strongQueue.size() == weakQueue.size());
    }

    public int size()
    {
        checkSize();
        return strongQueue.size();
    }

    public boolean isEmpty()
    {
        checkSize();
        return size() == 0;
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("strongQueueOrder", stream(true)
                        .map(RedstoneSignal::getStrongLevel).toArray())
                .add("weakQueueOrder", stream(false)
                        .map(RedstoneSignal::getWeakLevel).toArray())
                .add("signals", new ArrayList<>(strongQueue))
                .add("idToSignals", idToSignals)
                .toString();
    }
}
