package lumien.randomthings.entitys;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lumien.randomthings.config.Features;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.recipes.goldenchicken.GoldenChickenRecipe;
import lumien.randomthings.recipes.goldenchicken.GoldenChickenRecipeHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
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
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityGoldenChicken extends EntityAnimal {
    private static final DataParameter<ItemStack> EATING_ITEM = EntityDataManager
            .createKey(EntityGoldenChicken.class, DataSerializers.ITEM_STACK);

    public float wingRotation;
    public float destPos;
    public float oFlapSpeed;
    public float oFlap;
    public float wingRotDelta = 1.0F;
    /** The time until the next egg is spawned. */
    public int timeUntilNextIngotEgg;
    public int ingotDropTimer;
    public boolean chickenJockey;

    private ItemStack pendingOutput = ItemStack.EMPTY;

    private int eggDropTime = 6000;

    public EntityGoldenChicken(World worldIn) {
        super(worldIn);
        this.timeUntilNextIngotEgg = this.rand.nextInt(eggDropTime) + eggDropTime;
        this.setSize(0.4F, 0.7F);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(EATING_ITEM, ItemStack.EMPTY);
    }

    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIPanic(this, 1.4D));
        this.tasks.addTask(3, new EntityAIGoldenChickenTempt(this, 1.0D, false));
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

    public ItemStack getEatingItem() {
        return this.dataManager.get(EATING_ITEM);
    }

    public void setEatingItem(ItemStack stack) {
        this.dataManager.set(EATING_ITEM, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
        this.dataManager.setDirty(EATING_ITEM);
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);
        if (this.world.isRemote && EATING_ITEM.equals(key)) {
            ItemStack stack = this.getEatingItem();
            if (!stack.isEmpty()) {
                this.spawnEatingParticles(stack, 2);
            }
        }
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

        if (this.world.isRemote) {
            this.updateEatingParticles();
            return;
        }

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
        // Even if Golden Chicken Production is enabled, we also drop items when fed
        // a matching recipe input

        if (ingotDropTimer > 0 && --this.ingotDropTimer <= 0) {
            this.playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1.0F,
                    (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.5F);
            ItemStack output = this.pendingOutput.isEmpty() ? new ItemStack(Items.GOLD_INGOT, 3)
                    : this.pendingOutput.copy();
            this.entityDropItem(output, 0.0F);
            this.pendingOutput = ItemStack.EMPTY;
            this.setEatingItem(ItemStack.EMPTY);
        }

        if (this.ingotDropTimer == 0) {
            List<EntityItem> items = this.world.getEntitiesWithinAABB(EntityItem.class,
                    this.getEntityBoundingBox().grow(0.5));

            if (items.size() > 0) {
                for (EntityItem ei : items) {
                    ItemStack stack = ei.getItem();

                    if (!stack.isEmpty()) {
                        GoldenChickenRecipe recipe = GoldenChickenRecipeHandler.findRecipe(stack);
                        if (recipe != null) {
                            ItemStack eaten = stack.copy();
                            eaten.setCount(1);
                            stack.shrink(1);
                            ei.setItem(stack);

                            this.pendingOutput = recipe.getOutput();
                            this.setEatingItem(eaten);
                            this.ingotDropTimer = recipe.rollDigestTime(this.rand);
                            this.playSound(SoundEvents.ENTITY_GENERIC_EAT,
                                    0.5F + 0.5F * (float) this.rand.nextInt(2),
                                    (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void updateEatingParticles() {
        ItemStack eating = this.getEatingItem();
        if (eating.isEmpty() || this.ticksExisted % 4 != 0) {
            return;
        }

        // Player eating is 5 particles every 4 ticks. About 50% of that is 2-3.
        int count = 2 + (this.rand.nextBoolean() ? 1 : 0);
        this.spawnEatingParticles(eating, count);

        if (this.ticksExisted % 8 == 0) {
            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.5F + 0.5F * (float) this.rand.nextInt(2),
                    (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
        }
    }

    private void spawnEatingParticles(ItemStack stack, int particleCount) {
        for (int i = 0; i < particleCount; ++i) {
            Vec3d velocity = new Vec3d(((double) this.rand.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D,
                    0.0D);
            velocity = velocity.rotatePitch(-this.rotationPitch * 0.017453292F);
            velocity = velocity.rotateYaw(-this.rotationYawHead * 0.017453292F);

            double d0 = (double) (-this.rand.nextFloat()) * 0.4D - 0.1D;
            Vec3d pos = new Vec3d(((double) this.rand.nextFloat() - 0.5D) * 0.2D, d0, 0.35D);
            pos = pos.rotatePitch(-this.rotationPitch * 0.017453292F);
            pos = pos.rotateYaw(-this.rotationYawHead * 0.017453292F);
            pos = pos.add(this.posX, this.posY + (double) this.height * 0.90D, this.posZ);

            this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, pos.x, pos.y, pos.z, velocity.x,
                    velocity.y + 0.05D, velocity.z, Item.getIdFromItem(stack.getItem()), stack.getMetadata());
        }
    }

    public void fall(float distance, float damageMultiplier) {
        // Golden Chicken is a chicken, so we don't allow it to take damage from falling
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_CHICKEN_AMBIENT;
    }

    protected SoundEvent getHurtSound(@Nonnull DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_CHICKEN_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_CHICKEN_DEATH;
    }

    protected void playStepSound(@Nonnull BlockPos pos, @Nonnull Block blockIn) {
        this.playSound(SoundEvents.ENTITY_CHICKEN_STEP, 0.15F, 1.2F);
    }

    @Nullable
    protected ResourceLocation getLootTable() {
        return LootTableList.ENTITIES_CHICKEN;
    }

    public EntityGoldenChicken createChild(@Nonnull EntityAgeable ageable) {
        return new EntityGoldenChicken(this.world);
    }


    // Golden Chicken cannot be bred, so we don't allow it to be fed to breed it
    public boolean isBreedingItem(@Nonnull ItemStack stack) {
        return false;
    }

    public static void registerFixesChicken(DataFixer fixer) {
        EntityLiving.registerFixesMob(fixer, EntityGoldenChicken.class);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(@Nonnull NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.chickenJockey = compound.getBoolean("IsChickenJockey");
        this.timeUntilNextIngotEgg = compound.getInteger("timeUntilNextIngotEgg");
        this.ingotDropTimer = compound.getInteger("ingotDropTimer");
        if (compound.hasKey("pendingOutput")) {
            this.pendingOutput = new ItemStack(compound.getCompoundTag("pendingOutput"));
        }
        if (compound.hasKey("eatingItem")) {
            this.setEatingItem(new ItemStack(compound.getCompoundTag("eatingItem")));
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(@Nonnull NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("IsChickenJockey", this.chickenJockey);
        compound.setInteger("timeUntilNextIngotEgg", this.timeUntilNextIngotEgg);
        compound.setInteger("ingotDropTimer", this.ingotDropTimer);
        if (!this.pendingOutput.isEmpty()) {
            compound.setTag("pendingOutput", this.pendingOutput.writeToNBT(new NBTTagCompound()));
        }
        ItemStack eating = this.getEatingItem();
        if (!eating.isEmpty()) {
            compound.setTag("eatingItem", eating.writeToNBT(new NBTTagCompound()));
        }
    }
}
