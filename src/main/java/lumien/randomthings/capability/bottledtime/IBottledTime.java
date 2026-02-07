package lumien.randomthings.capability.bottledtime;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import lumien.randomthings.capability.ICapability;
import lumien.randomthings.lib.Reference;

public interface IBottledTime extends ICapability<IBottledTime> {
    @CapabilityInject(IBottledTime.class)
    Capability<IBottledTime> CAPABILITY_BOTTLED_TIME = null;

    ResourceLocation CAPABILITY_BOTTLED_TIME_KEY = new ResourceLocation(Reference.MOD_ID, "bottled_time");

    long getBottledTime();

    void setBottledTime(long bottledTime);

    // Only used to detect multiple bottles. Not persistent
    long getLastAddedWorldTime();

    void setLastAddedWorldTime(long worldTime);
}
