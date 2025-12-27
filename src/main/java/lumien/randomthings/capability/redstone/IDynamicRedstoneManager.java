package lumien.randomthings.capability.redstone;

import javax.annotation.Nonnull;
import java.util.EnumSet;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import lumien.randomthings.capability.ICapability;
import lumien.randomthings.capability.redstone.IDynamicRedstone.Source;
import lumien.randomthings.lib.Reference;

/**
 * Capability that manages externally controlled, position-based redstone signals.
 */
public interface IDynamicRedstoneManager extends ICapability<IDynamicRedstoneManager>
{
    @CapabilityInject(IDynamicRedstoneManager.class)
    Capability<IDynamicRedstoneManager> CAPABILITY_DYNAMIC_REDSTONE = null;

    ResourceLocation CAPABILITY_DYNAMIC_REDSTONE_KEY = new ResourceLocation(Reference.MOD_ID, "capability_dynamic_redstone");

    /**
     * @return If there are any signals providing redstone power.
     */
    boolean hasDynamicSignals();

    /**
     * @return If there are any signals to tick.
     */
    boolean hasTickingSignals();

    /**
     * Get the dynamic redstone power at a given position.
     * @param pos The position of the redstone power.
     * @param side The side from which the power should come from.
     * @param allowedSources The set of allowed {@link Source}s the dynamic redstone can handle.
     * @return The dynamic redstone power.
     */
    IDynamicRedstone getDynamicRedstone(BlockPos pos, @Nonnull EnumFacing side, EnumSet<Source> allowedSources);

    void tick();
}
