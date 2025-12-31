package lumien.randomthings.capability.redstone;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import lumien.randomthings.capability.ICapability;
import lumien.randomthings.handler.redstone.source.IDynamicRedstoneSource;
import lumien.randomthings.handler.redstone.source.RedstoneSource;
import lumien.randomthings.lib.Reference;

/**
 * Capability that manages externally controlled, position-based redstone signals.
 */
public interface IDynamicRedstoneManager extends ICapability<IDynamicRedstoneManager>, ITaskScheduler
{
    @CapabilityInject(IDynamicRedstoneManager.class)
    Capability<IDynamicRedstoneManager> CAPABILITY_DYNAMIC_REDSTONE = null;

    ResourceLocation CAPABILITY_DYNAMIC_REDSTONE_KEY = new ResourceLocation(Reference.MOD_ID, "capability_dynamic_redstone");

    /**
     * @return The world this manager is attached to.
     */
    @Nonnull
    World getWorld();

    /**
     * @return If there are any signals providing redstone power.
     */
    boolean hasDynamicSignals();

    /**
     * Get the dynamic redstone power at a given position.
     * @param pos The position of the redstone power.
     * @param side The side from which the power should come from.
     * @param signalBlock The signal's source block, if it exists.
     * @param allowedSources The set of allowed {@link RedstoneSource.Type}s the dynamic redstone can handle.
     * @return The dynamic redstone power.
     */
    @Nonnull
    IDynamicRedstone getDynamicRedstone(BlockPos pos, @Nonnull EnumFacing side, @Nullable Block signalBlock, EnumSet<RedstoneSource.Type> allowedSources);

    /* Observer functions */

    /**
     * Update all observers observing a specified position.
     * @param observedPos The position being observed.
     * @param state The blockstate of the position.
     * @param observerBlock The type of observer block.
     */
    void updateObservers(BlockPos observedPos, IBlockState state, Block observerBlock);

    /**
     * Start observing a specified position for an observer.
     * @param pos The position of the observer.
     * @param observer The {@link IDynamicRedstoneSource} info describing the observer.
     */
    void startObserving(BlockPos pos, IDynamicRedstoneSource observer);

    /**
     * Stop observing a specified position for an observer.
     * @param pos The position of the observer.
     * @param observer The {@link IDynamicRedstoneSource} info describing the observer.
     */
    void stopObserving(BlockPos pos, IDynamicRedstoneSource observer);

    /* Ticking */

    /**
     * @return If there are any signals to tick.
     */
    boolean hasTickingSignals();

    /**
     * Tick any tickable signals.
     */
    void tick();
}
