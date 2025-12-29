package lumien.randomthings.handler.redstone.component;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * Interface for reading dynamic redstone signals.
 */
public interface IRedstoneReader extends IRedstoneComponent
{
    /**
     * @param pos The signal's position.
     * @param side The signal's side.
     * @param strongPower If the signal's strong power should be read, otherwise its weak power.
     * @return The strong or weak power of the dynamic redstone signal.
     */
    int getRedstoneLevel(BlockPos pos, EnumFacing side, boolean strongPower);
}