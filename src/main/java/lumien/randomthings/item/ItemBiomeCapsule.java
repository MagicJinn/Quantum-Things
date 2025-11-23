package lumien.randomthings.item;

import javax.annotation.Nullable;
import lumien.randomthings.RandomThings;
import lumien.randomthings.config.Numbers;
import lumien.randomthings.entitys.EntityBiomeCapsule;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemBiomeCapsule extends ItemBase {
    private int currentCapacity = 0;

    public ItemBiomeCapsule() {
        super("biomeCapsule");
        this.setMaxStackSize(1);
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Override
    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        EntityBiomeCapsule item = new EntityBiomeCapsule(world, location.posX, location.posY,
                location.posZ, itemstack);
        item.setPickupDelay(40);
        item.motionX = location.motionX;
        item.motionY = location.motionY;
        item.motionZ = location.motionZ;
        return item;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        // TODO: Implement and remove WIP
        return super.getItemStackDisplayName(stack) + " (WIP)";
    }

    public int GetMaxCapacity() {
        return Numbers.MAX_BIOME_CAPSULE_CAPACITY;
    }

    public int GetCurrentCapacity() {
        return currentCapacity;
    }

    @Override
    public int getEntityLifespan(ItemStack stack, World world) {
        return Integer.MAX_VALUE;
    }
}
