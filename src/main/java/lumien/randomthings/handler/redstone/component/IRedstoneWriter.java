package lumien.randomthings.handler.redstone.component;

import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * Interface for writing dynamic redstone signals.
 */
public interface IRedstoneWriter extends IRedstoneComponent
{
    /**
     * Writes the dynamic redstone signal.
     * @param block The signal's block.
     * @param pos The signal's position.
     * @param side The signal's side.
     * @param weakLevel The signal's weak power level.
     * @param strongLevel The signal's strong power level.
     */
    void setRedstoneLevel(Block block, BlockPos pos, EnumFacing side, int weakLevel, int strongLevel);

    /**
     * Removes the dynamic redstone signal at the specified position and side.
     * @param block The signal's block.
     * @param pos The signal's position.
     * @param side The signal's side.
     */
    void deactivate(Block block, BlockPos pos, EnumFacing side);
}