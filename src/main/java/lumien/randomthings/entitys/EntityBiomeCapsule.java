package lumien.randomthings.entitys;

import java.awt.Color;
import javax.annotation.Nonnull;
import lumien.randomthings.client.particles.EntityColoredSmokeFX;
import lumien.randomthings.config.Numbers;
import lumien.randomthings.util.client.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class EntityBiomeCapsule extends EntityItem {
    /**
     * Biome Capsule Entity
     * 
     * When dropped on the ground, it will start charging up at 1/second. Its charges start at -10,
     * so the first 10 seconds it will not charge up. After that, it picks a biome and starts
     * charging. It will never despawn.
     **/

    public static final String NBT_HELD_CHARGES = "heldCharges";
    public static final String NBT_HELD_BIOME = "heldBiome";

    private static DataParameter<Integer> heldCharges =
            EntityDataManager.createKey(EntityBiomeCapsule.class, DataSerializers.VARINT);
    private static DataParameter<String> heldBiome =
            EntityDataManager.createKey(EntityBiomeCapsule.class, DataSerializers.STRING);

    public EntityBiomeCapsule(World worldIn) {
        super(worldIn);
    }

    public EntityBiomeCapsule(World worldIn, double x, double y, double z, ItemStack stack) {
        super(worldIn, x, y, z, stack);
        if (stack.getTagCompound() != null) {
            this.dataManager.set(heldCharges, stack.getTagCompound().getInteger(NBT_HELD_CHARGES));
            if (stack.getTagCompound().hasKey(NBT_HELD_BIOME)) {
                this.dataManager.set(heldBiome, stack.getTagCompound().getString(NBT_HELD_BIOME));
            }
        }
        // If no biome when created, set charges to -10
        if (getHeldBiome() == null) {
            this.dataManager.set(heldCharges, -10);
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(heldCharges, 0);
        this.getDataManager().register(heldBiome, "");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);

        compound.setInteger(NBT_HELD_CHARGES, getHeldCharges());
        String biomeString = getHeldBiomeString();
        if (biomeString != null && !biomeString.isEmpty()) {
            compound.setString(NBT_HELD_BIOME, biomeString);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);

        setHeldCharges(compound.getInteger(NBT_HELD_CHARGES));
        if (compound.hasKey(NBT_HELD_BIOME)) {
            setHeldBiomeString(compound.getString(NBT_HELD_BIOME));
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        Biome biomeHere = this.getBiomeHere();
        boolean isInHeldBiome = this.getHeldBiome() == biomeHere;

        // runs every second
        if ((this.world.getTotalWorldTime() + this.getEntityId()) % 20L != 0L) {
            return;
        }

        int heldCharges = this.getHeldCharges();

        // Client-side particle spawning
        if (this.world.isRemote) {
            if (isInHeldBiome) {
                spawnChargingParticles(biomeHere, heldCharges);
            }
            // Always return if client-side
            return;
        }


        if (this.getHeldBiome() == null) {
            if (heldCharges >= 0) {
                // Set biome if none set and charges are above 0
                this.setHeldBiome(biomeHere);
                this.setHeldCharges(0);
            }
        } else if (!isInHeldBiome) {
            // Only increment charges if the biome is the same as the held biome
            return;
        }

        // Increment charges
        if (heldCharges < Numbers.MAX_BIOME_CAPSULE_CAPACITY) {
            this.setHeldCharges(heldCharges + 1);
        }
    }

    @SideOnly(Side.CLIENT)
    private void spawnChargingParticles(@Nonnull Biome biomeHere, int heldCharges) {
        int charges = this.getHeldCharges();
        if (charges < Numbers.MAX_BIOME_CAPSULE_CAPACITY && charges >= 0) {
            // Spawn colored particles if charging
            int intColor = RenderUtils.getBiomeColor(null, biomeHere,
                    new BlockPos(this.posX, this.posY, this.posZ));
            Color c = new Color(intColor);
            double modColor = 1.0D / 255.0D;
            for (int i = 0; i < 10; i++) {
                EntityColoredSmokeFX particle = new EntityColoredSmokeFX(this.world,
                        this.posX + (this.world.rand.nextDouble() - 0.5D) * 0.3D,
                        this.posY + 0.2D + this.world.rand.nextDouble() * 0.2D,
                        this.posZ + (this.world.rand.nextDouble() - 0.5D) * 0.3D, 0.0D, 0.0D, 0.0D);
                particle.setRBGColorF((float) (modColor * c.getRed()),
                        (float) (modColor * c.getGreen()), (float) (modColor * c.getBlue()));
                Minecraft.getMinecraft().effectRenderer.addEffect(particle);
            }
        }
    }

    public int getHeldCharges() {
        return this.dataManager.get(heldCharges);
    }

    public void setHeldCharges(int newCharges) {
        this.dataManager.set(heldCharges, newCharges);
    }

    /**
     * Gets the biome as a Biome object.
     * 
     * @return The Biome object, or null if not set or invalid
     */
    public Biome getHeldBiome() {
        String biomeString = getHeldBiomeString();
        if (biomeString != null && !biomeString.isEmpty()) {
            return Biome.REGISTRY.getObject(new ResourceLocation(biomeString));
        }
        return null;
    }

    /**
     * Gets the biome as a string (ResourceLocation format).
     * 
     * @return The biome string, or empty string if not set
     */
    public String getHeldBiomeString() {
        return this.dataManager.get(heldBiome);
    }

    /**
     * Sets the biome using a Biome object.
     * 
     * @param biome The Biome to set
     */
    public void setHeldBiome(Biome biome) {
        if (biome != null) {
            ResourceLocation biomeRegistryName = Biome.REGISTRY.getNameForObject(biome);
            if (biomeRegistryName != null) {
                setHeldBiomeString(biomeRegistryName.toString());
            }
        } else {
            setHeldBiomeString("");
        }
    }

    /**
     * Sets the biome using a string (ResourceLocation format).
     * 
     * @param biomeString The biome string to set
     */
    public void setHeldBiomeString(String biomeString) {
        this.dataManager.set(heldBiome, biomeString != null ? biomeString : "");
    }

    public Biome getBiomeHere() {
        return this.world.getBiome(new BlockPos(this.posX, this.posY, this.posZ));
    }

    @Override
    public ItemStack getItem() {
        ItemStack stack = super.getItem();
        if (!stack.isEmpty()) {
            NBTTagCompound compound = stack.getTagCompound();
            if (compound == null) {
                compound = new NBTTagCompound();
                stack.setTagCompound(compound);
            }

            // If it gets picked up with still no biome, set it to 0 so the bar doesn't look weird
            int charges = getHeldCharges();
            if (getHeldBiome() == null) {
                charges = 0;
            }

            // Sync charges and biome from entity data manager to ItemStack NBT
            compound.setInteger(NBT_HELD_CHARGES, charges);
            String biomeString = getHeldBiomeString();
            if (biomeString != null && !biomeString.isEmpty()) {
                compound.setString(NBT_HELD_BIOME, biomeString);
            } else {
                compound.removeTag(NBT_HELD_BIOME);
            }
        }
        return stack;
    }
}
