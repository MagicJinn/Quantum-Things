package lumien.randomthings.handler.redstone.scheduling;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import net.minecraft.util.math.BlockPos;

import com.google.common.base.MoreObjects;

/**
 * A group of tasks associated with a required chunk and source chunk.
 */
public class TaskGroup implements Runnable
{
    private final Set<Task> tasks;

    public TaskGroup()
    {
        this.tasks = new HashSet<>();
    }

    public void addTask(BlockPos requiredPos, BlockPos sourcePos, Runnable task)
    {
        Task taskToAdd = new Task(requiredPos, sourcePos, task);
        // Overwrite the old task with same requiredPos, sourcePos
        tasks.remove(taskToAdd);
        tasks.add(taskToAdd);
    }

    @Override
    public void run()
    {
        for (Task task : tasks)
        {
            task.run();
        }
        tasks.clear();
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("tasks", tasks)
                .toString();
    }

    private static class Task implements Runnable
    {
        final BlockPos requiredPos;
        final BlockPos sourcePos;
        final Runnable task;

        private Task(BlockPos requiredPos, BlockPos sourcePos, Runnable task)
        {
            this.requiredPos = requiredPos;
            this.sourcePos = sourcePos;
            this.task = task;
        }

        @Override
        public void run()
        {
            task.run();
        }

        @Override
        public boolean equals(Object o)
        {
            if (!(o instanceof Task)) return false;
            Task task = (Task) o;
            return Objects.equals(requiredPos, task.requiredPos) && Objects.equals(sourcePos, task.sourcePos);
        }

        @Override
        public int hashCode()
        {
            int hash = requiredPos.hashCode();
            hash = 31 * hash + sourcePos.hashCode();
            return hash;
        }

        @Override
        public String toString()
        {
            return MoreObjects.toStringHelper(this)
                    .add("requiredPos", requiredPos)
                    .add("sourcePos", sourcePos)
                    .toString();
        }
    }
}
