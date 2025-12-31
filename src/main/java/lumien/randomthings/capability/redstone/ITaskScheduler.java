package lumien.randomthings.capability.redstone;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import lumien.randomthings.handler.redstone.scheduling.ChunkArea;

/**
 * Implement this interface when you need a scheduler that runs tasks requiring a certain area to be loaded.
 * The task should come from an in-world source, such that scheduled tasks can be canceled when the source
 * is invalidated.
 */
public interface ITaskScheduler
{
    /**
     * Schedule a task that may run on chunk load.
     * @param requiredArea The area with the required chunk.
     * @param sourcePos The task's source block position.
     * @param task The task to schedule.
     */
    void scheduleTask(ChunkArea requiredArea, BlockPos sourcePos, Runnable task);

    /**
     * Remove any scheduled tasks for the chunk that was just unloaded.
     * @param unloadedChunkPos The unloaded chunk's position.
     */
    void invalidateTasks(ChunkPos unloadedChunkPos);

    /**
     * Run scheduled tasks for the chunk that was just loaded.
     * Each task should only run if its {@link ChunkArea} is completely loaded.
     * @param loadedChunkPos The loaded chunk's position.
     */
    void runScheduledTasks(ChunkPos loadedChunkPos);
}
