package lumien.randomthings.handler;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import lumien.randomthings.capability.ICapability;
import lumien.randomthings.capability.bottledtime.BottledTimeProvider;
import lumien.randomthings.capability.bottledtime.BottledTimeStorage;
import lumien.randomthings.capability.bottledtime.IBottledTime;
import lumien.randomthings.handler.redstone.DynamicRedstoneManager;
import lumien.randomthings.capability.redstone.DynamicRedstoneManagerProvider;
import lumien.randomthings.capability.redstone.IDynamicRedstoneManager;

public class CapabilityHandler
{
    public CapabilityHandler()
    {
        registerCapability(IDynamicRedstoneManager.class, new DynamicRedstoneManager());
        registerCapability(IBottledTime.class, new BottledTimeStorage());
    }

    public <T extends ICapability<T>> void registerCapability(Class<T> clazz, T defaultImpl)
    {
        CapabilityManager.INSTANCE.register(clazz, new Capability.IStorage<T>()
                {
                    @Nonnull
                    @Override
                    public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side)
                    {
                        NBTTagCompound nbt = new NBTTagCompound();
                        return instance.writeNBT(instance, side, nbt);
                    }

                    @Override
                    public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt)
                    {
                        if (nbt instanceof NBTTagCompound) {
                            instance.readNBT(instance, side, (NBTTagCompound) nbt);
                        }
                    }
                }, () -> defaultImpl);
    }

    @SubscribeEvent
    public void attachWorldCapability(AttachCapabilitiesEvent<World> event)
    {
        if (!event.getCapabilities().containsKey(IDynamicRedstoneManager.CAPABILITY_DYNAMIC_REDSTONE_KEY))
        {
            event.addCapability(IDynamicRedstoneManager.CAPABILITY_DYNAMIC_REDSTONE_KEY, new DynamicRedstoneManagerProvider(event.getObject()));
        }
    }

    @SubscribeEvent
    public void attachEntityCapability(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof EntityPlayer && !event.getCapabilities().containsKey(IBottledTime.CAPABILITY_BOTTLED_TIME_KEY))
        {
            event.addCapability(IBottledTime.CAPABILITY_BOTTLED_TIME_KEY, new BottledTimeProvider());
        }
    }
}
