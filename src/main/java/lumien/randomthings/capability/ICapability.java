package lumien.randomthings.capability;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public interface ICapability<T extends ICapability<T>>
{
    @Nonnull
    NBTTagCompound writeNBT(T instance, EnumFacing side, NBTTagCompound nbt);

    void readNBT(T instance, EnumFacing side, NBTTagCompound nbt);
}
