package lumien.randomthings.capability.redstone;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.world.BlockEvent;

import lumien.randomthings.handler.RTEventHandler;
import lumien.randomthings.handler.redstone.signal.RedstoneSignal;
import lumien.randomthings.item.ItemRedstoneActivator;
import lumien.randomthings.item.ItemRedstoneRemote;
import lumien.randomthings.tileentity.TileEntityRedstoneObserver;
import lumien.randomthings.tileentity.redstoneinterface.TileEntityRedstoneInterface;

/**
 * An external way to control redstone levels for a block.
 */
public interface IDynamicRedstone
{
    /**
     * Use this redstone level to indicate this signal should be removed from its manager.
     */
    int REMOVE_SIGNAL = -1;

    /**
     * @return The redstone level.
     */
    int getRedstoneLevel();

    /**
     * Set the redstone signal.
     *
     * @param signalIn The {@link RedstoneSignal}.
     */
    void setRedstoneLevel(RedstoneSignal signalIn);

    /**
     * @return If the redstone power is a strong signal.
     */
    boolean isStrongSignal();

    enum Source
    {
        /**
         * From {@link TileEntityRedstoneInterface}.
         */
        INTERFACE(0),
        /**
         * From {@link RTEventHandler#notifyNeighbors(BlockEvent.NeighborNotifyEvent)},
         * used with {@link TileEntityRedstoneObserver}.
         */
        OBSERVEE(1),
        /**
         * From an item ({@link ItemRedstoneActivator}, {@link ItemRedstoneRemote}).
         */
        ITEM(2)
        ;

        private final int index;

        public static final String SOURCE_KEY = "source";
        public static final Source[] VALUES = new Source[3];

        Source(int index)
        {
            this.index = index;
        }

        public int getIndex()
        {
            return index;
        }

        public static Source byIndex(int index)
        {
            return VALUES[MathHelper.abs(index % VALUES.length)];
        }

        static
        {
            for (Source value : values())
            {
                VALUES[value.index] = value;
            }
        }
    }
}
