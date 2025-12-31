package lumien.randomthings.handler.redstone.scheduling;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lumien.randomthings.capability.redstone.ITaskScheduler;

public class TaskScheduler implements ITaskScheduler
{
    private World world;
    /* Row: Required chunk, Column: Source chunk, Value: Tasks to run when required chunk is loaded */
    private final Table<ChunkPos, ChunkPos, TaskGroup> scheduledTasks;

    public TaskScheduler()
    {
        scheduledTasks = HashBasedTable.create();
    }

    public void setWorld(World world)
    {
        this.world = world;
    }

    @Override
    public void scheduleTask(ChunkArea requiredArea, BlockPos sourcePos, Runnable task)
    {
        ChunkPos requiredChunk = requiredArea.getRequiredChunk();
        ChunkPos sourceChunk = new ChunkPos(sourcePos);
        TaskGroup taskGroup = scheduledTasks.get(requiredChunk, sourceChunk);
        if (taskGroup == null)
        {
            taskGroup = new TaskGroup(requiredArea);
            scheduledTasks.put(requiredChunk, sourceChunk, taskGroup);
        }
        taskGroup.addTask(task);
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
        Collection<TaskGroup> requiredChunkTasks = scheduledTasks.row(loadedChunkPos).values();
        Iterator<TaskGroup> itr = requiredChunkTasks.iterator();
        while (itr.hasNext())
        {
            TaskGroup taskGroup = itr.next();
            // Whole area, not just this chunk, must be loaded
            if (taskGroup.isAreaLoaded(world))
            {
                taskGroup.runTasks();
                itr.remove();
            }
        }
    }
}
