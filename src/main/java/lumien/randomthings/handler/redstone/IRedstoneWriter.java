package lumien.randomthings.handler.redstone;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * Interface for writing dynamic redstone signals.
 */
public interface IRedstoneWriter extends IRedstoneConnectionProvider
{
    void setRedstoneLevel(BlockPos pos, EnumFacing side, int level, boolean strongPower);

    void deactivate(BlockPos pos, EnumFacing side);
}
