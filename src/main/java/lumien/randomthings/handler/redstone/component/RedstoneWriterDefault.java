package lumien.randomthings.handler.redstone.component;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import lumien.randomthings.capability.redstone.IDynamicRedstone;
import lumien.randomthings.capability.redstone.IDynamicRedstoneManager;
import lumien.randomthings.handler.redstone.signal.RedstoneSignal;
import lumien.randomthings.handler.redstone.signal.RemovalSignal;
import lumien.randomthings.handler.redstone.source.IDynamicRedstoneSource;
import lumien.randomthings.util.Lazy;

public class RedstoneWriterDefault implements IRedstoneWriter
{
    private final Lazy<Optional<IDynamicRedstoneManager>> manager;
    private final IDynamicRedstoneSource source;

    public RedstoneWriterDefault(@Nonnull Lazy<Optional<IDynamicRedstoneManager>> manager, IDynamicRedstoneSource source)
    {
        this.manager = manager;
        this.source = source;
    }

    public RedstoneWriterDefault(@Nonnull IDynamicRedstoneManager manager, IDynamicRedstoneSource source)
    {
        this(Lazy.of(() -> Optional.of(manager)), source);
    }

    @Nonnull
    @Override
    public Optional<IDynamicRedstone> getDynamicRedstoneFor(Block block, BlockPos pos, EnumFacing side)
    {
        return manager.get()
                .map(manager -> manager.getDynamicRedstone(pos.offset(side), side, block, EnumSet.of(source.getType())));
    }

    @Override
    public void setRedstoneLevel(Block block, BlockPos pos, EnumFacing side, int weakLevel, int strongLevel)
    {
        getDynamicRedstoneFor(block, pos, side).ifPresent(dynamicRedstone ->
                dynamicRedstone.setRedstoneLevel(new RedstoneSignal(source, weakLevel, strongLevel)));
    }

    @Override
    public void deactivate(Block block, BlockPos pos, EnumFacing side)
    {
        getDynamicRedstoneFor(block, pos, side).ifPresent(dynamicRedstone ->
                dynamicRedstone.setRedstoneLevel(new RemovalSignal(source, dynamicRedstone.isStrongSignal())));
    }
}
