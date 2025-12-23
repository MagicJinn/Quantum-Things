package lumien.randomthings.handler.redstone.signal;

public interface ITickableSignal
{
    /**
     * @return The current age of the signal in ticks.
     */
    int getAge();

    /**
     * @return How long the signal can last in ticks.
     */
    int getDuration();

    /**
     * Ticks this signal's age.
     */
    void tick();

    /**
     * @return If the signal is still alive.
     */
    boolean isAlive();
}
