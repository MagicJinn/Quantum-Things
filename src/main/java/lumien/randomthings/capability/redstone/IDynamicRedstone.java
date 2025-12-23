package lumien.randomthings.capability.redstone;

import lumien.randomthings.handler.redstone.signal.RedstoneSignal;

/**
 * An external way to control redstone levels for a block.
 */
public interface IDynamicRedstone
{
    /**
     * Use this redstone level to indicate this signal should be removed from its manager.
     */
    int REMOVE_SIGNAL = -1;

    /**
     * @return The redstone level.
     */
    int getRedstoneLevel();

    /**
     * Set the redstone signal.
     *
     * @param signalIn The {@link RedstoneSignal}.
     * @param strongPower If the redstone power is a strong signal.
     */
    void setRedstoneLevel(RedstoneSignal signalIn, boolean strongPower);

    /**
     * @return If the redstone power is a strong signal.
     */
    boolean isStrongSignal();
}
