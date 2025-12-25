package lumien.randomthings.handler.redstone.signal;

import net.minecraft.util.EnumFacing;

import lumien.randomthings.capability.redstone.IDynamicRedstone;
import lumien.randomthings.util.DimPos;

public interface IPositionedSignal
{
    /**
     * @return The position of this signal.
     */
    DimPos getPos();

    /**
     * @return The side this signal is emitting to.
     */
    EnumFacing getSide();

    /**
     * @return The type of source of this signal.
     */
    IDynamicRedstone.Source getSourceType();
}
