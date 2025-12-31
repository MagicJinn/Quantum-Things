package lumien.randomthings.handler.redstone.scheduling;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.World;

public class TaskGroup
{
    private final ChunkArea requiredArea;
    private final List<Runnable> tasks;

    public TaskGroup(ChunkArea requiredArea)
    {
        this.requiredArea = requiredArea;
        this.tasks = new ArrayList<>();
    }

    public void addTask(Runnable task)
    {
        tasks.add(task);
    }

    public void runTasks()
    {
        for (Runnable task : tasks)
        {
            task.run();
        }
    }

    public boolean isAreaLoaded(World world)
    {
        return requiredArea.isAreaLoaded(world);
    }
}
