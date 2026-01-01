package lumien.randomthings.handler.redstone.scheduling;

import javax.annotation.Nonnull;
import java.util.Map;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class TaskScheduler implements ITaskScheduler
{
    /* Row: Required chunk, Column: Source chunk, Value: Tasks to run when required chunk is loaded */
    private final Table<ChunkPos, ChunkPos, TaskGroup> scheduledTasks;

    public TaskScheduler()
    {
        scheduledTasks = HashBasedTable.create();
    }

    @Override
    public void scheduleTask(@Nonnull BlockPos requiredPos, @Nonnull BlockPos sourcePos, Runnable task)
    {
        ChunkPos requiredChunk = new ChunkPos(requiredPos);
        ChunkPos sourceChunk = new ChunkPos(sourcePos);
        TaskGroup taskGroup = scheduledTasks.get(requiredChunk, sourceChunk);
        if (taskGroup == null)
        {
            taskGroup = new TaskGroup();
            scheduledTasks.put(requiredChunk, sourceChunk, taskGroup);
        }
        taskGroup.addTask(requiredPos, sourcePos, task);
    }

    @Override
    public void invalidateTasks(ChunkPos unloadedChunkPos)
    {
        if (!scheduledTasks.containsColumn(unloadedChunkPos)) return;

        // Unloaded chunk is the source chunk/column
        for (Map.Entry<ChunkPos, Map<ChunkPos, TaskGroup>> requiredChunkToTasks : scheduledTasks.rowMap().entrySet())
        {
            Map<ChunkPos, TaskGroup> sourceChunkToTasks = requiredChunkToTasks.getValue();
            sourceChunkToTasks.remove(unloadedChunkPos);
        }
    }

    @Override
    public void runScheduledTasks(ChunkPos loadedChunkPos)
    {
        if (!scheduledTasks.containsRow(loadedChunkPos)) return;

        // Loaded chunk is the required chunk/row
        Map<ChunkPos, TaskGroup> taskGroups = scheduledTasks.rowMap().remove(loadedChunkPos);
        for (TaskGroup taskGroup : taskGroups.values())
        {
            taskGroup.run();
        }
    }
}
