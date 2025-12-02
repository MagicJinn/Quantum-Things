package lumien.randomthings.entitys;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import lumien.randomthings.RandomThings;
import lumien.randomthings.config.Features;
import lumien.randomthings.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.oredict.OreDictionary;

public class EntityGoldenChicken extends EntityAnimal {
    private static final Set<Item> TEMPTATION_ITEMS = Sets.newHashSet(Items.WHEAT_SEEDS, Items.MELON_SEEDS,
            Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS);
    public float wingRotation;
    public float destPos;
    public float oFlapSpeed;
    public float oFlap;
    public float wingRotDelta = 1.0F;
    /** The time until the next egg is spawned. */
    public int timeUntilNextIngotEgg;
    public int ingotDropTimer;
    public boolean chickenJockey;

    private int eggDropTime = 6000;

    public EntityGoldenChicken(World worldIn) {
        super(worldIn);
        this.timeUntilNextIngotEgg = this.rand.nextInt(eggDropTime) + eggDropTime;
        this.setSize(0.4F, 0.7F);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
    }

    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIPanic(this, 1.4D));
        this.tasks.addTask(3, new EntityAITempt(this, 1.0D, false, TEMPTATION_ITEMS));
        this.tasks.addTask(4, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(5, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
    }

    public float getEyeHeight() {
        return this.height;
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(4.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.oFlap = this.wingRotation;
        this.oFlapSpeed = this.destPos;
        this.destPos = (float) ((double) this.destPos + (double) (this.onGround ? -1 : 4) * 0.3D);
        this.destPos = MathHelper.clamp(this.destPos, 0.0F, 1.0F);

        if (!this.onGround && this.wingRotDelta < 1.0F) {
            this.wingRotDelta = 1.0F;
        }

        this.wingRotDelta = (float) ((double) this.wingRotDelta * 0.9D);

        if (!this.onGround && this.motionY < 0.0D) {
            this.motionY *= 0.6D;
        }

        this.wingRotation += this.wingRotDelta * 2.0F;

        if (this.world.isRemote)
            return;

        if (Features.GOLDEN_CHICKEN_PRODUCTION)
        // Produce gold like eggs
        {
            if (--timeUntilNextIngotEgg <= 0) {
                this.playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1.0F,
                        (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.5F);
                this.timeUntilNextIngotEgg = this.rand.nextInt(eggDropTime) + eggDropTime;
                if (world.rand.nextInt(100) == 0) {
                    this.entityDropItem(new ItemStack(ModItems.ingredients, 1, 11), 0.0F);
                } else {
                    this.dropItem(Items.GOLD_INGOT, 1);
                }
            }
        }
        // Even if Golden Chicken Production is enabled, we also drop ingots when fed
        // gold ore

        if (ingotDropTimer > 0 && --this.ingotDropTimer <= 0) {
            this.playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1.0F,
                    (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.5F);
            this.dropItem(Items.GOLD_INGOT, 3);
        }

        if (this.ingotDropTimer == 0) {
            List<EntityItem> items = this.world.getEntitiesWithinAABB(EntityItem.class,
                    this.getEntityBoundingBox().grow(0.5));

            if (items.size() > 0) {
                int goldOreID = OreDictionary.getOreID("oreGold");
                for (EntityItem ei : items) {
                    ItemStack stack = ei.getItem();

                    if (!stack.isEmpty()) {
                        int[] ids = OreDictionary.getOreIDs(stack);

                        boolean found = false;
                        for (int i : ids) {
                            if (i == goldOreID) {
                                stack.shrink(1);
                                ei.setItem(stack);

                                found = true;
                                break;
                            }
                        }

                        if (found) {
                            this.ingotDropTimer = 600 + this.rand.nextInt(600);
                            break;
                        }
                    }
                }
            }
        }
    }

    public void fall(float distance, float damageMultiplier) {
        // Golden Chicken is a chicken, so we don't allow it to take damage from falling
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_CHICKEN_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_CHICKEN_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_CHICKEN_DEATH;
    }

    protected void playStepSound(BlockPos pos, Block blockIn) {
        this.playSound(SoundEvents.ENTITY_CHICKEN_STEP, 0.15F, 1.2F);
    }

    @Nullable
    protected ResourceLocation getLootTable() {
        return LootTableList.ENTITIES_CHICKEN;
    }

    public EntityGoldenChicken createChild(EntityAgeable ageable) {
        return new EntityGoldenChicken(this.world);
    }


    // Golden Chicken cannot be bred, so we don't allow it to be fed to breed it
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    public static void registerFixesChicken(DataFixer fixer) {
        EntityLiving.registerFixesMob(fixer, EntityGoldenChicken.class);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.chickenJockey = compound.getBoolean("IsChickenJockey");
        this.timeUntilNextIngotEgg = compound.getInteger("timeUntilNextIngotEgg");
        this.ingotDropTimer = compound.getInteger("ingotDropTimer");
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("IsChickenJockey", this.chickenJockey);
        compound.setInteger("timeUntilNextIngotEgg", this.timeUntilNextIngotEgg);
        compound.setInteger("ingotDropTimer", this.ingotDropTimer);
    }
}