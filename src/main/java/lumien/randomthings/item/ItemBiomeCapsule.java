package lumien.randomthings.item;

import java.awt.Color;
import java.util.List;
import lumien.randomthings.config.Numbers;
import lumien.randomthings.entitys.EntityBiomeCapsule;
import lumien.randomthings.lib.IRTItemColor;
import lumien.randomthings.util.client.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBiomeCapsule extends ItemBase implements IRTItemColor {
    // store biome here somehow

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
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1 - (double) getHeldCharges(stack) / Numbers.MAX_BIOME_CAPSULE_CAPACITY;
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return Color.GREEN.getRGB();
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        // TODO: Implement and remove WIP
        return super.getItemStackDisplayName(stack) + " (WIP)";
    }

    public int getHeldCharges(ItemStack stack) {
        NBTTagCompound compound;
        if ((compound = stack.getTagCompound()) != null
                && compound.hasKey(EntityBiomeCapsule.NBT_HELD_CHARGES)) {
            return compound.getInteger(EntityBiomeCapsule.NBT_HELD_CHARGES);
        }
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip,
            ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        tooltip.add(
                "Charges: " + getHeldCharges(stack) + " / " + Numbers.MAX_BIOME_CAPSULE_CAPACITY);
        Biome biome = getBiome(stack);
        if (biome != null) {
            tooltip.add("Biome: " + biome.getBiomeName());
        } else {
            tooltip.add("Biome: None");
        }
    }

    @Override
    public int getEntityLifespan(ItemStack stack, World world) {
        return Integer.MAX_VALUE;
    }

    /**
     * Gets the biome from the ItemStack NBT.
     * 
     * @param stack The ItemStack to get the biome from
     * @return The Biome object, or null if not set or invalid
     */
    public static Biome getBiome(ItemStack stack) {
        NBTTagCompound compound;

        if ((compound = stack.getTagCompound()) != null
                && compound.hasKey(EntityBiomeCapsule.NBT_HELD_BIOME)) {
            String biomeName = compound.getString(EntityBiomeCapsule.NBT_HELD_BIOME);

            Biome biome = Biome.REGISTRY.getObject(new ResourceLocation(biomeName));

            return biome;
        }

        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemstack(ItemStack stack, int tintIndex) {
        Biome biome;

        if ((biome = getBiome(stack)) != null) {
            return RenderUtils.getBiomeColor(null, biome,
                    Minecraft.getMinecraft().player.getPosition());
        }

        return Color.WHITE.getRGB();
    }
}
