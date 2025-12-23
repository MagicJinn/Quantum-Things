package lumien.randomthings.capability.redstone;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import lumien.randomthings.handler.redstone.DynamicRedstoneManager;

public class DynamicRedstoneManagerProvider implements ICapabilitySerializable<NBTBase>
{
    private final IDynamicRedstoneManager manager;

    public DynamicRedstoneManagerProvider(int dimId)
    {
        this.manager = new DynamicRedstoneManager(dimId);
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing enumFacing)
    {
        return capability == IDynamicRedstoneManager.CAPABILITY_DYNAMIC_REDSTONE;
    }

    @SuppressWarnings("ConstantValue")
    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing enumFacing)
    {
        if (capability == IDynamicRedstoneManager.CAPABILITY_DYNAMIC_REDSTONE)
        {
            return IDynamicRedstoneManager.CAPABILITY_DYNAMIC_REDSTONE.cast(manager);
        }
        return null;
    }


    @Override
    public NBTBase serializeNBT()
    {
        return IDynamicRedstoneManager.CAPABILITY_DYNAMIC_REDSTONE.writeNBT(manager, null);
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void deserializeNBT(NBTBase nbt)
    {
        IDynamicRedstoneManager.CAPABILITY_DYNAMIC_REDSTONE.readNBT(manager, null, nbt);
    }
}
