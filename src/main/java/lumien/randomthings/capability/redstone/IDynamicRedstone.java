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
     * Return the redstone level, weak or strong.
     * @param strongPower If the level returned should be the strong power.
     * @return The redstone level.
     */
    int getRedstoneLevel(boolean strongPower);

    /**
     * Set the redstone signal.
     *
     * @param signalIn The {@link RedstoneSignal}.
     */
    void setRedstoneLevel(RedstoneSignal signalIn);

    /**
     * @return If the redstone power is a strong signal.
     */
    boolean isStrongSignal();
}
