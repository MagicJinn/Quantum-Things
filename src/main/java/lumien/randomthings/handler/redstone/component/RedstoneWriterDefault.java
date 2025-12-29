package lumien.randomthings.handler.redstone.component;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.Optional;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import lumien.randomthings.capability.redstone.IDynamicRedstone;
import lumien.randomthings.capability.redstone.IDynamicRedstoneManager;
import lumien.randomthings.handler.redstone.signal.RedstoneSignal;
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
        this.manager = Lazy.of(() -> Optional.of(manager));
        this.source = source;
    }

    @Nonnull
    @Override
    public Optional<IDynamicRedstone> getDynamicRedstoneFor(BlockPos pos, EnumFacing side)
    {
        return manager.get()
                .map(manager -> manager.getDynamicRedstone(pos.offset(side), side, EnumSet.of(source.getType())));
    }

    @Override
    public void setRedstoneLevel(BlockPos pos, EnumFacing side, int weakLevel, int strongLevel)
    {
        getDynamicRedstoneFor(pos, side).ifPresent(dynamicRedstone ->
                dynamicRedstone.setRedstoneLevel(new RedstoneSignal(source, weakLevel, strongLevel)));
    }

    @Override
    public void deactivate(BlockPos pos, EnumFacing side)
    {
        getDynamicRedstoneFor(pos, side).ifPresent(dynamicRedstone ->
                dynamicRedstone.setRedstoneLevel(new RedstoneSignal(source, IDynamicRedstone.REMOVE_SIGNAL, dynamicRedstone.isStrongSignal())));
    }
}
