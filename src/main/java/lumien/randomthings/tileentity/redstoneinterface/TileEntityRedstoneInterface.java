package lumien.randomthings.tileentity.redstoneinterface;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumMap;
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
import lumien.randomthings.handler.redstone.IRedstoneConnectionProvider;
import lumien.randomthings.handler.redstone.component.IRedstoneWriter;
import lumien.randomthings.handler.redstone.signal.RedstoneSignal;
import lumien.randomthings.handler.redstone.signal.RemovalSignal;
import lumien.randomthings.handler.redstone.source.IDynamicRedstoneSource;
import lumien.randomthings.handler.redstone.source.RedstoneSource;
import lumien.randomthings.tileentity.TileEntityBase;
import lumien.randomthings.util.Lazy;
import lumien.randomthings.util.WorldUtil;

import static lumien.randomthings.handler.redstone.source.RedstoneSource.SOURCE_KEY;

public abstract class TileEntityRedstoneInterface extends TileEntityBase implements IDynamicRedstoneSource, IRedstoneConnectionProvider, IRedstoneWriter
{
    public static final EnumSet<RedstoneSource.Type> INTERFACE_SOURCE = EnumSet.of(RedstoneSource.Type.INTERFACE);

    @Nonnull
    private Lazy<Optional<IDynamicRedstoneManager>> redstoneManager;
    @Nonnull
    protected UUID sourceId;

    public TileEntityRedstoneInterface()
    {
        redstoneManager = Lazy.empty();
        sourceId = UUID.randomUUID();
    }

    protected abstract Set<BlockPos> getTargets();

    @Override
    public void onLoad()
    {
        if (world.isRemote) return;

        redstoneManager = Lazy.ofCapability(world, IDynamicRedstoneManager.CAPABILITY_DYNAMIC_REDSTONE);
        sendSignal(getTargets());
    }

    @Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block neighborBlock, BlockPos changedPos)
	{
        if (world.isRemote) return;

		BlockPos direction = changedPos.subtract(pos);
        EnumFacing side = EnumFacing.getFacingFromVector(direction.getX(), direction.getY(), direction.getZ());
        sendSidedSignal(side, getTargets());
	}

    /**
     * Sends the signal received by this interface to its targets on the specified side.
     * @param signalDir The side the received signal is coming from, relative to this tile.
     * @param targets The targets' positions.
     */
    public void sendSidedSignal(EnumFacing signalDir, Set<BlockPos> targets)
    {
        if (targets.isEmpty()) return;

        BlockPos signalPos = pos.offset(signalDir);
        Block signalBlock = world.getBlockState(signalPos).getBlock();
        // May cause chunks neighboring the interface to load, but should be fine.
        int weakLevel = world.getRedstonePower(signalPos, signalDir);
        int strongLevel = world.getStrongPower(signalPos, signalDir);

        for (BlockPos targetPos : targets)
        {
            Block block = world.getBlockState(targetPos).getBlock();
            if (weakLevel > 0 || strongLevel > 0)
            {
                setRedstoneLevel(signalBlock, targetPos, signalDir, weakLevel, strongLevel);
            }
            else
            {
                deactivate(signalBlock, targetPos, signalDir);
            }
            // Update target's neighbors
            if (WorldUtil.isNeighboring(targetPos, pos))
            {
                // Don't recursively update this interface
                world.notifyNeighborsOfStateExcept(targetPos, block, signalDir.getOpposite());
            }
            else
            {
                world.notifyNeighborsOfStateChange(targetPos, block, false);
            }
        }
    }

    /**
     * Sends the signal received by this interface to its targets for all sides.
     * @param targets The targets' positions.
     */
    public void sendSignal(Set<BlockPos> targets)
    {
        if (targets.isEmpty()) return;

        EnumMap<EnumFacing, Block> signalBlocks = new EnumMap<>(EnumFacing.class);
        EnumMap<EnumFacing, Integer> weakLevels = new EnumMap<>(EnumFacing.class);
        EnumMap<EnumFacing, Integer> strongLevels = new EnumMap<>(EnumFacing.class);
        // Gather observed signal levels
        for (EnumFacing side : EnumFacing.VALUES)
        {
            BlockPos signalPos = pos.offset(side);
            Block signalBlock = world.getBlockState(signalPos).getBlock();
            int weakLevel = world.getRedstonePower(signalPos, side);
            int strongLevel = world.getStrongPower(signalPos, side);

            signalBlocks.put(side, signalBlock);
            weakLevels.put(side, weakLevel);
            strongLevels.put(side, strongLevel);
        }
        for (BlockPos targetPos : targets)
        {
            Block block = world.getBlockState(targetPos).getBlock();
            for (EnumFacing side : EnumFacing.VALUES)
            {
                Block signalBlock = signalBlocks.get(side);
                int weakLevel = weakLevels.get(side);
                int strongLevel = strongLevels.get(side);

                if (weakLevel > 0 || strongLevel > 0)
                {
                    setRedstoneLevel(signalBlock, targetPos, side, weakLevel, strongLevel);
                }
                else
                {
                    deactivate(signalBlock, targetPos, side);
                }
            }
            // Update target's neighbors
            world.notifyNeighborsOfStateChange(targetPos, block, false);
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

    protected void invalidateTargets(Set<BlockPos> targets)
    {
        if (world.isRemote || targets.isEmpty()) return;

        EnumMap<EnumFacing, Block> signalBlocks = new EnumMap<>(EnumFacing.class);
        for (EnumFacing side : EnumFacing.VALUES)
        {
            BlockPos signalPos = pos.offset(side);
            Block signalBlock = world.getBlockState(signalPos).getBlock();
            signalBlocks.put(side, signalBlock);
        }
        for (BlockPos targetPos : targets)
        {
            Block block = world.getBlockState(targetPos).getBlock();
            for (EnumFacing side : EnumFacing.VALUES)
            {
                deactivate(signalBlocks.get(side), targetPos, side);
            }
            // Update target's neighbors
            world.notifyNeighborsOfStateChange(targetPos, block, false);
        }
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
            UUID sourceId = compound.getUniqueId(SOURCE_KEY);
            Preconditions.checkNotNull(sourceId);
            this.sourceId = sourceId;
        }
        else
        {
            sourceId = UUID.randomUUID();
        }
    }

    /* Dynamic redstone */

    @Nonnull
    @Override
    public Optional<IDynamicRedstone> getDynamicRedstoneFor(Block block, BlockPos pos, EnumFacing side)
    {
        return redstoneManager.get()
                .map(manager -> manager.getDynamicRedstone(pos.offset(side), side, block, INTERFACE_SOURCE));
    }

    @Override
    public void setRedstoneLevel(Block block, BlockPos pos, EnumFacing side, int weakLevel, int strongLevel)
    {
        getDynamicRedstoneFor(block, pos, side).ifPresent(dynamicRedstone ->
                dynamicRedstone.setRedstoneLevel(new RedstoneSignal(TileEntityRedstoneInterface.this, weakLevel, strongLevel)));
    }

    @Override
    public void deactivate(Block block, BlockPos pos, EnumFacing side)
    {
        getDynamicRedstoneFor(block, pos, side).ifPresent(dynamicRedstone ->
                dynamicRedstone.setRedstoneLevel(new RemovalSignal(TileEntityRedstoneInterface.this, dynamicRedstone.isStrongSignal())));
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
