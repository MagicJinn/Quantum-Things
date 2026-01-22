package lumien.randomthings.handler.redstone.scheduling;

import javax.annotation.Nonnull;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

/**
 * Implement this interface when you need a scheduler for tasks requiring a certain area to be loaded.
 * A source can only have one scheduled task active per required position.
 */
public interface ITaskScheduler
{
    /**
     * Schedule a task that may run when the required position loads.
     * If this source already scheduled a task, overwrites the old task.
     * @param requiredPos The position required to be loaded for the task.
     * @param sourcePos The task's source block position.
     * @param task The task to schedule.
     */
    void scheduleTask(@Nonnull BlockPos requiredPos, @Nonnull BlockPos sourcePos, Runnable task);

    /**
     * Remove any scheduled tasks sourced from the chunk that was just unloaded.
     * @param unloadedChunkPos The unloaded chunk's position.
     */
    void invalidateTasks(ChunkPos unloadedChunkPos);

    /**
     * Run scheduled tasks for the chunk that was just loaded.
     * @param loadedChunkPos The loaded chunk's position.
     */
    void runScheduledTasks(ChunkPos loadedChunkPos);
}
