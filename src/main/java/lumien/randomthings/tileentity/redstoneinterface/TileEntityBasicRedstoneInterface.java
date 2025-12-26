package lumien.randomthings.tileentity.redstoneinterface;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class TileEntityBasicRedstoneInterface extends TileEntityRedstoneInterface
{
	private BlockPos target;

    @Override
    public void writeDataToNBT(NBTTagCompound compound, boolean sync)
    {
        super.writeDataToNBT(compound, sync);

        if (target != null)
        {
            compound.setInteger("targetX", target.getX());
            compound.setInteger("targetY", target.getY());
            compound.setInteger("targetZ", target.getZ());
        }
    }

    @Override
    public void readDataFromNBT(NBTTagCompound compound, boolean sync)
    {
        super.readDataFromNBT(compound, sync);

        if (compound.hasKey("targetX"))
        {
            target = new BlockPos(compound.getInteger("targetX"), compound.getInteger("targetY"), compound.getInteger("targetZ"));
        }
    }

    @Nullable
    public BlockPos getTarget()
    {
        return this.target;
    }

    @Override
    protected Set<BlockPos> getTargets()
    {
        return target == null ? Collections.emptySet() : Collections.singleton(target);
    }

    public void setTarget(BlockPos newTarget)
    {
        if (newTarget.equals(target) || world.isRemote) return;

        BlockPos oldTarget = this.target;

        this.target = newTarget;
        IBlockState state = this.world.getBlockState(this.pos);
        this.world.notifyBlockUpdate(pos, state, state, 3);

        if (oldTarget != null)
        {
            invalidateTargets(Collections.singleton(oldTarget));
        }

        for (EnumFacing side : EnumFacing.VALUES)
        {
            sendSignal(side, getTargets());
        }
    }
}
