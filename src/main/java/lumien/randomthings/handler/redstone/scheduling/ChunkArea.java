package lumien.randomthings.handler.redstone.scheduling;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

/**
 * A chunk area, consisting of the chunk of a required block and the chunks of its neighbors (including diagonals).
 * The maximal area depends on the radius, but here's a visual:
 * <pre>
 * {@code
 * -----------------------------------------------------
 * | requiredOrthogonalChunk | neighborOrthogonalChunk |
 * -----------------------------------------------------
 * | requiredChunk           | neighborChunk           |
 * -----------------------------------------------------
 * }
 * </pre>
 * An example use case is a radius of 1 where the required position is on the corner of {@code requiredChunk}.
 */
public class ChunkArea
{
    private final BlockPos requiredPos;
    private final int radius;

    private ChunkArea(BlockPos requiredPos, int radius)
    {
        this.requiredPos = requiredPos;
        this.radius = radius;
    }

    public static ChunkArea of(BlockPos requiredPos)
    {
        return new ChunkArea(requiredPos, 0);
    }

    public static ChunkArea of(BlockPos requiredPos, int radius)
    {
        return new ChunkArea(requiredPos, radius);
    }

    public ChunkPos getRequiredChunk()
    {
        return new ChunkPos(requiredPos);
    }

    public boolean isAreaLoaded(World world)
    {
        return world.isAreaLoaded(requiredPos, radius);
    }
}
