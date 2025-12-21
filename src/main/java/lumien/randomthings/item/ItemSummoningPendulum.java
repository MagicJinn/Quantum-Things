package lumien.randomthings.item;

import java.util.List;

import org.lwjgl.input.Keyboard;

import lumien.randomthings.config.Features;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemSummoningPendulum extends ItemBase
{
	private static final int MAX_ENTITIES = 5;

	public ItemSummoningPendulum()
	{
		super("summoningPendulum");

		/*
		 * ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, new
		 * SingleRandomChestContent(new ItemStack(this), 1, 1, 1));
		 * ChestGenHooks.addItem(ChestGenHooks.STRONGHOLD_CORRIDOR, new
		 * SingleRandomChestContent(new ItemStack(this), 1, 1, 30)); TODO
		 */

		this.setMaxStackSize(1);
	}

	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack)
	{
		return EnumDyeColor.PURPLE.getColorValue();
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack)
	{
		int entityCount = 0;

		NBTTagCompound compound = stack.getTagCompound();
		if (compound != null)
		{
			NBTTagList tagList = compound.getTagList("entitys", 10);
			entityCount = tagList.tagCount();
		}

		return 1 - 1F / MAX_ENTITIES * Math.min(entityCount, MAX_ENTITIES);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack)
	{
		int entityCount = 0;

		NBTTagCompound compound = stack.getTagCompound();
		if (compound != null)
		{
			NBTTagList tagList = compound.getTagList("entitys", 10);
			entityCount = tagList.tagCount();
		}

		return entityCount == MAX_ENTITIES;
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack)
	{
		int entityCount = 0;

		NBTTagCompound compound = stack.getTagCompound();
		if (compound != null)
		{
			NBTTagList tagList = compound.getTagList("entitys", 10);
			entityCount = tagList.tagCount();
		}

		return entityCount != MAX_ENTITIES;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced)
	{
		int entityCount = 0;

		NBTTagCompound compound = stack.getTagCompound();
		if (compound != null)
		{
			NBTTagList tagList = compound.getTagList("entitys", 10);
			entityCount = tagList.tagCount();
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && entityCount != 0)
		{
			NBTTagList tagList = compound.getTagList("entitys", 10);

			for (int i = 0; i < tagList.tagCount(); i++)
			{
				ResourceLocation entityLocation = new ResourceLocation(tagList.getCompoundTagAt(i).getString("id"));
				EntityEntry entry = ForgeRegistries.ENTITIES.getValue(entityLocation);

				if (entry != null)
				{
					tooltip.add("- " + I18n.format("entity." + entry.getName() + ".name"));
				}
			}
			return;
		}
		tooltip.add(I18n.format(entityCount == 1 ? "tooltip.summoningPendulum.entityCount.singular" : "tooltip.summoningPendulum.entityCount.plural", entityCount));
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack itemstack, EntityPlayer player, EntityLivingBase entity, EnumHand hand)
	{
		if (entity.world.isRemote)
			return false;

		itemstack = player.getHeldItemMainhand();
		NBTTagCompound compound = itemstack.getTagCompound();
		if (compound == null) {
			compound = new NBTTagCompound();
		}

		NBTTagList tagList = compound.getTagList("entitys", 10);

		// Early return if any fail condition is met (unless creative)
		if (!player.isCreative()
				&& ((entity instanceof IMob || entity instanceof EntityPlayer) || isEntityBlacklisted(entity)
						|| !ownsEntity(entity, player) || isTargetingYou(entity, player)
						|| tagList.tagCount() >= MAX_ENTITIES)) {
			entity.world.playSound(null, player.getPosition(), SoundEvents.ITEM_FIRECHARGE_USE,
					SoundCategory.PLAYERS, 0.5f, 1.5F);

			if (tagList.tagCount() >= MAX_ENTITIES) {
				compound.setTag("entitys", tagList);
				itemstack.setTagCompound(compound);
			}
			return true;
		}

		// Capture the entity
		NBTTagCompound entityNBT = new NBTTagCompound();
		entity.writeToNBTOptional(entityNBT);
		tagList.appendTag(entityNBT);
		entity.setDead();
		entity.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS,
				0.5f, 1.5F);

		compound.setTag("entitys", tagList);
		itemstack.setTagCompound(compound);
		return true;
	}

	private boolean isTargetingYou(Entity entity, EntityPlayer player) {
		if (entity instanceof EntityLivingBase) {
			EntityLiving living = (EntityLiving) entity;
			boolean isTargeting = living.getAttackTarget() == player || living.getRevengeTarget() == player;
			return isTargeting;
		}
		return false;
	}

	private boolean ownsEntity(Entity entity, EntityPlayer player) {
		if (entity instanceof EntityTameable) {
			EntityTameable tameable = (EntityTameable) entity;
			// Disallow stealing pets
			if (tameable.isTamed())
				return tameable.isOwner(player);
		}
		return true; // Default to true, so if the entity is not tameable, it can be captured
	}

	private boolean isEntityBlacklisted(Entity entity) {
		ResourceLocation entityKey = EntityList.getKey(entity);
		if (entityKey != null) {
			String entityId = entityKey.toString();
			for (String blacklistedId : Features.SUMMONING_PENDULUM_BLACKLIST) {
				if (entityId.equals(blacklistedId)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		ItemStack stack = playerIn.getHeldItem(hand);
		pos = pos.offset(side);
		if (!worldIn.isRemote)
		{
			NBTTagCompound compound = stack.getTagCompound();
			if (compound != null)
			{
				NBTTagList tagList = compound.getTagList("entitys", 10);
				if (tagList.tagCount() > 0)
				{
					NBTTagCompound entityNBT = tagList.getCompoundTagAt(0);
					tagList.removeTag(0);
					
					entityNBT.setInteger("Dimension", worldIn.provider.getDimension());
					
					Entity entity = EntityList.createEntityFromNBT(entityNBT, worldIn);
					if (entity != null)
					{
						entity.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
						worldIn.spawnEntity(entity);
						playerIn.world.playSound(null, playerIn.getPosition(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 0.5f, 0.5F);
					}
				}
				else
				{
					playerIn.world.playSound(null, playerIn.getPosition(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 0.5f, 0.2F);
				}
			}
		}
		return EnumActionResult.SUCCESS;
	}

	@Override
	public EnumRarity getRarity(ItemStack stack)
	{
		return EnumRarity.RARE;
	}
}
