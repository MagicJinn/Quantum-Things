package lumien.randomthings.handler.redstone;

import net.minecraft.util.math.BlockPos;

public class Connection
{
    private final BlockPos sourcePos;
    private final BlockPos targetPos;

    public Connection(BlockPos sourcePos, BlockPos targetPos)
    {
        this.sourcePos = sourcePos;
        this.targetPos = targetPos;
    }

    public BlockPos source()
    {
        return sourcePos;
    }

    public BlockPos target()
    {
        return targetPos;
    }
}
