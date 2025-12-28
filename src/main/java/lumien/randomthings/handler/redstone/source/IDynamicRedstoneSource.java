package lumien.randomthings.handler.redstone.source;

import java.util.UUID;

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
}
