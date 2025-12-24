package lumien.randomthings.handler.redstone;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * Interface for reading dynamic redstone signals.
 */
public interface IRedstoneReader extends IRedstoneConnectionProvider
{
    void getRedstoneLevel(BlockPos pos, EnumFacing side);
}
