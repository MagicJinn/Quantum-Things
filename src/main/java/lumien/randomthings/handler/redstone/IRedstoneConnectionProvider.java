package lumien.randomthings.handler.redstone;

import java.util.List;

/**
 * Interface for a provider that associates dynamic redstone signals to a source.
 */
public interface IRedstoneConnectionProvider
{
    List<Connection> getConnections();
}
