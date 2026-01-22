package lumien.randomthings.handler.redstone.source;

import javax.annotation.Nullable;
import java.util.UUID;

import net.minecraft.util.math.BlockPos;

/**
 * A dynamic redstone source that is uniquely identifiable.
 */
public interface IDynamicRedstoneSource
{
    /**
     * @return The type of source.
     */
    RedstoneSource.Type getType();

    /**
     * @return The source's unique identifier.
     */
    UUID getId();

    /**
     * @return The source's position, if it exists.
     */
    /*
    This can't be named getPos() in order to implement it on tile entities - getPos() will be obfuscated away by
    them and crash with AbstractMethodError.
    */
    @Nullable
    BlockPos getSourcePos();
}
