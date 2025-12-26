package lumien.randomthings.handler.redstone.signal;

import net.minecraft.util.EnumFacing;

import lumien.randomthings.capability.redstone.IDynamicRedstoneManager;
import lumien.randomthings.util.DimPos;

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
     */
    void onRemoved(IDynamicRedstoneManager manager, DimPos pos, EnumFacing side);
}
