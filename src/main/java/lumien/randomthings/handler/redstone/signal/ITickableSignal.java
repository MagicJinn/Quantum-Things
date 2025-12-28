package lumien.randomthings.handler.redstone.signal;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import lumien.randomthings.capability.redstone.IDynamicRedstoneManager;

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

    /**
     * Callback for when this signal is removed from a manager.
     * @param manager The manager the signal was removed from.
     * @param pos The signal's position.
     * @param side The signal's side.
     */
    void onRemoved(IDynamicRedstoneManager manager, BlockPos pos, EnumFacing side);
}
