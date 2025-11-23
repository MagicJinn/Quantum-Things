package lumien.randomthings.entitys;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

public class EntityBiomeCapsule extends EntityItem {
    private static DataParameter<Integer> currentCapacity =
            EntityDataManager.createKey(EntityBiomeCapsule.class, DataSerializers.VARINT);

    public EntityBiomeCapsule(World worldIn) {
        super(worldIn);
    }

    public EntityBiomeCapsule(World worldIn, double x, double y, double z, ItemStack stack) {
        super(worldIn, x, y, z, stack);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(currentCapacity, 0);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);

        compound.setInteger("currentCapacity", getCurrentCapacity());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);

        setCurrentCapacity(compound.getInteger("currentCapacity"));
    }

    public int getCurrentCapacity() {
        return this.dataManager.get(currentCapacity);
    }

    public void setCurrentCapacity(int newCapacity) {
        this.dataManager.set(currentCapacity, newCapacity);
    }
}
