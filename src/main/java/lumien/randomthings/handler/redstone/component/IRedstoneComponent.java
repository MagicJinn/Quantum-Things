package lumien.randomthings.handler.redstone.component;

import javax.annotation.Nonnull;
import java.util.Optional;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import lumien.randomthings.capability.redstone.IDynamicRedstone;

/**
 * Interface to be implemented by something that uses dynamic redstone.
 */
public interface IRedstoneComponent
{
    /**
     * Get a dynamic redstone handle for a specified position and side.
     * @param pos The signal's position.
     * @param side The signal's side.
     * @return An {@link Optional} containing the dynamic redstone handle.
     */
    @Nonnull
    Optional<IDynamicRedstone> getDynamicRedstoneFor(BlockPos pos, EnumFacing side);
}
