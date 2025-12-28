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
import java.util.stream.StreamSupport;

/**
 * A queue of redstone signals, sorted by their strength.
 * Equality is determined solely by the signals' source.
 */
public class SignalQueue extends AbstractQueue<RedstoneSignal>
{
    private final PriorityQueue<RedstoneSignal> signalQueue;
    // Signals mapped by their unique id.
    private final Map<UUID, RedstoneSignal> signalMap;

    public SignalQueue()
    {
        signalQueue = new PriorityQueue<>(Comparator.comparingInt(RedstoneSignal::getRedstoneLevel).reversed());
        signalMap = new HashMap<>();
    }

    public RedstoneSignal getBySource(UUID sourceId)
    {
        return signalMap.get(sourceId);
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
        if (signalMap.containsKey(id)) {
            RedstoneSignal oldSignal = signalMap.get(id);
            super.remove(oldSignal);
        }
        signalQueue.offer(signal);
        signalMap.put(id, signal);
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
        RedstoneSignal toRemove = signalMap.remove(signalId);
        return signalQueue.remove(toRemove);
    }

    @Override
    public void clear()
    {
        signalQueue.clear();
        signalMap.clear();
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
        signalMap.remove(signalId);
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
}
