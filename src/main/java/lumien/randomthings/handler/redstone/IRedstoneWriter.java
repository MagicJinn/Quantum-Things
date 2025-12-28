package lumien.randomthings.handler.redstone;

import java.util.Optional;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import lumien.randomthings.capability.redstone.IDynamicRedstone;

/**
 * Interface for writing dynamic redstone signals.
 */
public interface IRedstoneWriter extends IRedstoneConnectionProvider
{
    Optional<IDynamicRedstone> getDynamicRedstoneFor(BlockPos pos, EnumFacing side);

    void setRedstoneLevel(BlockPos pos, EnumFacing side, int level, boolean strongPower);

    void deactivate(BlockPos pos, EnumFacing side);
}