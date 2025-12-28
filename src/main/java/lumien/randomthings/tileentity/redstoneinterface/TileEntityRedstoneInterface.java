package lumien.randomthings.tileentity.redstoneinterface;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.google.common.base.Preconditions;
import lumien.randomthings.capability.redstone.IDynamicRedstone;
import lumien.randomthings.capability.redstone.IDynamicRedstoneManager;
import lumien.randomthings.handler.redstone.Connection;
import lumien.randomthings.handler.redstone.IRedstoneWriter;
import lumien.randomthings.handler.redstone.signal.RedstoneSignal;
import lumien.randomthings.handler.redstone.source.IDynamicRedstoneSource;
import lumien.randomthings.handler.redstone.source.RedstoneSource;
import lumien.randomthings.tileentity.TileEntityBase;

import static lumien.randomthings.handler.redstone.source.RedstoneSource.SOURCE_KEY;
import static lumien.randomthings.handler.redstone.source.RedstoneSource.Type.INTERFACE;

public abstract class TileEntityRedstoneInterface extends TileEntityBase implements IDynamicRedstoneSource, IRedstoneWriter
{
    protected UUID sourceId;

    public TileEntityRedstoneInterface()
    {
        sourceId = UUID.randomUUID();
    }

    protected abstract Set<BlockPos> getTargets();

    @Override
    public void onLoad()
    {
        if (world.isRemote) return;
        for (EnumFacing side : EnumFacing.VALUES)
        {
            sendSignal(side, getTargets());
        }
    }

    @Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block neighborBlock, BlockPos changedPos)
	{
        if (world.isRemote) return;
		BlockPos direction = changedPos.subtract(pos);
        EnumFacing side = EnumFacing.getFacingFromVector(direction.getX(), direction.getY(), direction.getZ());
        sendSignal(side, getTargets());
	}

    /**
     * Sends the signal received by this interface to its targets.
     * @param signalDir The side the received signal is coming from, relative to this tile.
     */
    public void sendSignal(EnumFacing signalDir, Set<BlockPos> targets)
    {
        if (targets.isEmpty()) return;

        // May cause adjacent chunk loading, but that should be fine considering it's just direct neighbors and other redstone does this as well.
        BlockPos signalPos = pos.offset(signalDir);
        int strongLevel = world.getStrongPower(signalPos, signalDir);
        int level = world.getRedstonePower(signalPos, signalDir);

        for (BlockPos targetPos : targets)
        {
            if (level <= 0)
            {
                deactivate(targetPos, signalDir);
            }
            else
            {
                setRedstoneLevel(targetPos, signalDir, level, level == strongLevel);
            }
        }
    }

    @Override
    public void onChunkUnload()
    {
        super.onChunkUnload();
        invalidateTargets(getTargets());
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
        invalidateTargets(getTargets());
    }

    @Override
    public void writeDataToNBT(NBTTagCompound compound, boolean sync)
    {
        super.writeDataToNBT(compound, sync);

        compound.setUniqueId(SOURCE_KEY, sourceId);
    }

    @Override
    public void readDataFromNBT(NBTTagCompound compound, boolean sync)
    {
        super.readDataFromNBT(compound, sync);

        if (compound.hasUniqueId(SOURCE_KEY))
        {
            sourceId = compound.getUniqueId(SOURCE_KEY);
        }
        else
        {
            sourceId = UUID.randomUUID();
        }
    }

    /* Dynamic redstone */

    protected void invalidateTargets(Set<BlockPos> targets)
    {
        if (world.isRemote) return;
        for (EnumFacing side : EnumFacing.VALUES)
        {
            targets.forEach(pos -> deactivate(pos, side));
        }
    }

    @Nonnull
    @Override
    public Optional<IDynamicRedstone> getDynamicRedstoneFor(BlockPos pos, EnumFacing side)
    {
        IDynamicRedstoneManager managerCap = world.getCapability(IDynamicRedstoneManager.CAPABILITY_DYNAMIC_REDSTONE, null);
        return Optional.ofNullable(managerCap)
                .map(manager -> manager.getDynamicRedstone(pos.offset(side), side, EnumSet.of(INTERFACE)));
    }

    @Override
    public void setRedstoneLevel(BlockPos pos, EnumFacing side, int level, boolean strongPower)
    {
        getDynamicRedstoneFor(pos, side).ifPresent(dynamicRedstone ->
                dynamicRedstone.setRedstoneLevel(new RedstoneSignal(TileEntityRedstoneInterface.this, level, strongPower)));
    }

    @Override
    public void deactivate(BlockPos pos, EnumFacing side)
    {
        getDynamicRedstoneFor(pos, side).ifPresent(dynamicRedstone ->
                dynamicRedstone.setRedstoneLevel(new RedstoneSignal(TileEntityRedstoneInterface.this, IDynamicRedstone.REMOVE_SIGNAL, dynamicRedstone.isStrongSignal())));
    }

    @Override
    public RedstoneSource.Type getType()
    {
        return RedstoneSource.Type.INTERFACE;
    }

    @Override
    public UUID getId()
    {
        Preconditions.checkNotNull(sourceId);
        return sourceId;
    }

    /* Connection provider */

    @Override
    public List<Connection> getConnections()
    {
        List<Connection> connections = new ArrayList<>();
        for (BlockPos targetPos : getTargets())
        {
            Connection connection = new Connection(pos, targetPos);
            connections.add(connection);
        }
        return connections;
    }
}
