package lumien.randomthings.capability.bottledtime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class BottledTimeProvider implements ICapabilitySerializable<NBTBase> {
    private final IBottledTime storage = new BottledTimeStorage();

    @SuppressWarnings("ConstantValue")
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == IBottledTime.CAPABILITY_BOTTLED_TIME;
    }

    @SuppressWarnings("ConstantValue")
    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == IBottledTime.CAPABILITY_BOTTLED_TIME)
            return IBottledTime.CAPABILITY_BOTTLED_TIME.cast(storage);

        return null;
    }

    @Override
    public NBTBase serializeNBT() {
        return IBottledTime.CAPABILITY_BOTTLED_TIME.writeNBT(storage, null);
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void deserializeNBT(NBTBase nbt) {
        IBottledTime.CAPABILITY_BOTTLED_TIME.readNBT(storage, null, nbt);
    }
}
