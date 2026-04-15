package lumien.randomthings.item;

import java.util.Optional;

import lumien.randomthings.config.Features;
import lumien.randomthings.config.Numbers;
import lumien.randomthings.entitys.EntityTimeAccelerator;
import lumien.randomthings.capability.bottledtime.IBottledTime;
import net.minecraft.entity.player.EntityPlayerMP;
import lumien.randomthings.network.PacketHandler;
import lumien.randomthings.network.client.MessageBottledTimeSync;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemTimeInABottle extends ItemBase
{
	// Increased limit (68 years) but still safe enough to prevent overflow
	private static final long MAX_BOTTLED_TIME_TICKS = 42949672940L;

	// Legacy NBT from when time was stored on the item
	private static final String LEGACY_NBT_TIMEDATA = "timeData";
	private static final String LEGACY_NBT_STORED_TIME = "storedTime";
	private static final String BOTTLE_LAST_HOLDER = "lastHolder";
	private static final String BOTTLE_LAST_HOLDER_UUID = "uuid";
	private static final String BOTTLE_LAST_HOLDER_NAME = "name";

	public ItemTimeInABottle()
	{
		super("timeInABottle");

		this.setMaxStackSize(1);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		return !ItemStack.areItemsEqual(oldStack, newStack);
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
	{
		if (!worldIn.isRemote && entityIn instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entityIn;
			updateLastHolderMetadata(stack, player);

			if (Features.LEGACY_TIME_IN_A_BOTTLE) {
				runLegacyBottleUpdate(stack, worldIn, player);
				return;
			}

			IBottledTime cap = player.getCapability(IBottledTime.CAPABILITY_BOTTLED_TIME, null);
			if (cap == null) {
				return;
			}

			// Migrate old per-item NBT time into global capability when legacy mode is disabled.
			migrateLegacyNbtToCapability(stack, cap, player);

			int secondWorth = Numbers.TIME_IN_A_BOTTLE_SECOND;
			long worldTime = worldIn.getTotalWorldTime();
			boolean cycle = secondWorth == 0 || worldTime % secondWorth == 0;

			if (cycle && cap.getLastAddedWorldTime() != worldTime) {
				if (cap.getBottledTime() < MAX_BOTTLED_TIME_TICKS) {
					cap.setBottledTime(cap.getBottledTime() + 20);
					cap.setLastAddedWorldTime(worldTime);
					syncBottledTimeToClient(player);
				}
			}
		}
	}

	private void runLegacyBottleUpdate(ItemStack stack, World worldIn, EntityPlayer player)
	{
		migrateCapabilityToLegacyNbt(stack, player);

		int secondWorth = Numbers.TIME_IN_A_BOTTLE_SECOND;
		long worldTime = worldIn.getTotalWorldTime();
		boolean cycle = secondWorth == 0 || worldTime % secondWorth == 0;

		if (cycle) {
			long current = getLegacyStoredTime(stack);
			if (current < MAX_BOTTLED_TIME_TICKS) {
				setLegacyStoredTime(stack, Math.min(MAX_BOTTLED_TIME_TICKS, current + 20L));
			}
		}

		if (worldTime % 60 == 0) {
			for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
				ItemStack invStack = player.inventory.getStackInSlot(i);
				if (invStack.getItem() == this && invStack != stack) {
					long myTime = getLegacyStoredTime(stack);
					long theirTime = getLegacyStoredTime(invStack);
					if (myTime < theirTime) {
						setLegacyStoredTime(stack, 0);
					}
				}
			}
		}
	}

	// Migrates legacy NBT to the current capability system
	private void migrateLegacyNbtToCapability(ItemStack stack, IBottledTime cap, EntityPlayer player) {
		NBTTagCompound timeData = stack.getSubCompound(LEGACY_NBT_TIMEDATA);
		if (timeData == null || !timeData.hasKey(LEGACY_NBT_STORED_TIME))
			return;

		long legacyTicks = timeData.getInteger(LEGACY_NBT_STORED_TIME) & 0xFFFFFFFFL;
		if (legacyTicks <= 0)
			return;

		long current = cap.getBottledTime();
		long added = Math.min(legacyTicks, MAX_BOTTLED_TIME_TICKS - current);
		if (added > 0) {
			cap.setBottledTime(current + added);
			syncBottledTimeToClient(player);
		}

		// Clear legacy NBT so we don't migrate again. Remove compound if empty.
		timeData.removeTag(LEGACY_NBT_STORED_TIME);
		if (timeData.isEmpty()) {
			NBTTagCompound root = stack.getTagCompound();
			if (root != null)
				root.removeTag(LEGACY_NBT_TIMEDATA);
		}
	}

	// Moves global capability time into bottle NBT when legacy mode is active.
	private void migrateCapabilityToLegacyNbt(ItemStack stack, EntityPlayer player)
	{
		IBottledTime cap = player.getCapability(IBottledTime.CAPABILITY_BOTTLED_TIME, null);
		if (cap == null) {
			return;
		}

		long capabilityTicks = cap.getBottledTime();
		if (capabilityTicks > 0) {
			long currentBottle = getLegacyStoredTime(stack);
			long merged = Math.min(MAX_BOTTLED_TIME_TICKS, currentBottle + capabilityTicks);
			setLegacyStoredTime(stack, merged);
			cap.setBottledTime(0);
			syncBottledTimeToClient(player);
		}
	}

	private void updateLastHolderMetadata(ItemStack stack, EntityPlayer player)
	{
		NBTTagCompound root = stack.getTagCompound();
		if (root == null) {
			root = new NBTTagCompound();
			stack.setTagCompound(root);
		}

		NBTTagCompound holder = root.getCompoundTag(BOTTLE_LAST_HOLDER);
		if (player.getUniqueID() != null) {
			holder.setString(BOTTLE_LAST_HOLDER_UUID, player.getUniqueID().toString());
		}
		holder.setString(BOTTLE_LAST_HOLDER_NAME, player.getName());
		root.setTag(BOTTLE_LAST_HOLDER, holder);
	}

	public static Optional<String> getLastHolderUuid(ItemStack stack)
	{
		if (stack == null || stack.isEmpty() || !stack.hasTagCompound()) {
			return Optional.empty();
		}

		NBTTagCompound holder = stack.getTagCompound().getCompoundTag(BOTTLE_LAST_HOLDER);
		if (!holder.hasKey(BOTTLE_LAST_HOLDER_UUID)) {
			return Optional.empty();
		}

		String uuid = holder.getString(BOTTLE_LAST_HOLDER_UUID);
		return uuid == null || uuid.isEmpty() ? Optional.empty() : Optional.of(uuid);
	}

	public static Optional<String> getLastHolderName(ItemStack stack)
	{
		if (stack == null || stack.isEmpty() || !stack.hasTagCompound()) {
			return Optional.empty();
		}

		NBTTagCompound holder = stack.getTagCompound().getCompoundTag(BOTTLE_LAST_HOLDER);
		if (!holder.hasKey(BOTTLE_LAST_HOLDER_NAME)) {
			return Optional.empty();
		}

		String name = holder.getString(BOTTLE_LAST_HOLDER_NAME);
		return name == null || name.isEmpty() ? Optional.empty() : Optional.of(name);
	}

	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
	{
		if (!world.isRemote)
		{
			ItemStack me = player.getHeldItem(hand);

			Optional<EntityTimeAccelerator> o = world.getEntitiesWithinAABB(EntityTimeAccelerator.class, new AxisAlignedBB(pos).shrink(0.2)).stream().findFirst();

			if (o.isPresent())
			{
				EntityTimeAccelerator eta = o.get();

				int currentRate = eta.getTimeRate();

				int usedUpTime = 20 * 30 - eta.getRemainingTime();

				if (currentRate < 32)
				{
					int nextRate = currentRate * 2;

					int timeRequired = nextRate / 2 * 20 * 30;

					long timeAvailable = getStoredTime(me, player);

					if (timeAvailable >= timeRequired || player.capabilities.isCreativeMode)
					{
						int timeAdded = (nextRate * usedUpTime - currentRate * usedUpTime) / nextRate;

						if (!player.capabilities.isCreativeMode) {
							setStoredTime(me, player, timeAvailable - timeRequired);
						}

						eta.setTimeRate(nextRate);
						eta.setRemainingTime(eta.getRemainingTime() + timeAdded);

						float pitch = 1;
						
						switch (nextRate)
						{
							case 2:
								world.playSound(null, pos, SoundEvents.BLOCK_NOTE_HARP, SoundCategory.BLOCKS, 0.5F, 0.793701F);
								break;
							case 4:
								world.playSound(null, pos, SoundEvents.BLOCK_NOTE_HARP, SoundCategory.BLOCKS, 0.5F, 0.890899F);
								break;
							case 8:
								world.playSound(null, pos, SoundEvents.BLOCK_NOTE_HARP, SoundCategory.BLOCKS, 0.5F, 1.059463F);
								break;
							case 16:
								world.playSound(null, pos, SoundEvents.BLOCK_NOTE_HARP, SoundCategory.BLOCKS, 0.5F, 0.943874F);
								break;
							case 32:
								world.playSound(null, pos, SoundEvents.BLOCK_NOTE_HARP, SoundCategory.BLOCKS, 0.5F, 0.890899F);
								break;
						}
						
						// C# D E G F E
						return EnumActionResult.SUCCESS;
					}
				}
			}
			else
			{
				long timeAvailable = getStoredTime(me, player);

				if (timeAvailable >= 20 * 30 || player.capabilities.isCreativeMode)
				{
					if (!player.capabilities.isCreativeMode) {
						setStoredTime(me, player, timeAvailable - 20 * 30);
					}

					EntityTimeAccelerator n = new EntityTimeAccelerator(world, pos, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

					n.setTimeRate(1);
					n.setRemainingTime(20 * 30);

					world.playSound(null, pos, SoundEvents.BLOCK_NOTE_HARP, SoundCategory.BLOCKS, 0.5F, 0.749154F);
					world.spawnEntity(n);

					return EnumActionResult.SUCCESS;
				}
			}
		}

		return EnumActionResult.SUCCESS;
	}
	
	// Get the stored time from the player's capability
	public static long getStoredTime(EntityPlayer player)
	{
		return getStoredTime(ItemStack.EMPTY, player);
	}

	// Set the stored time in the player's capability
	public static void setStoredTime(EntityPlayer player, long time) {
		setStoredTime(ItemStack.EMPTY, player, time);
	}

	public static long getStoredTime(ItemStack stack, EntityPlayer player)
	{
		if (Features.LEGACY_TIME_IN_A_BOTTLE) {
			return getLegacyStoredTime(stack);
		}

		IBottledTime cap = player != null ? player.getCapability(IBottledTime.CAPABILITY_BOTTLED_TIME, null) : null;
		return cap != null ? cap.getBottledTime() : 0;
	}

	public static void setStoredTime(ItemStack stack, EntityPlayer player, long time)
	{
		long clamped = Math.max(0L, Math.min(MAX_BOTTLED_TIME_TICKS, time));
		if (Features.LEGACY_TIME_IN_A_BOTTLE) {
			setLegacyStoredTime(stack, clamped);
			return;
		}

		IBottledTime cap = player != null ? player.getCapability(IBottledTime.CAPABILITY_BOTTLED_TIME, null) : null;
		if (cap != null) {
			cap.setBottledTime(clamped);
			syncBottledTimeToClient(player);
		}
	}

	public static long getLegacyStoredTime(ItemStack stack)
	{
		if (stack == null || stack.isEmpty()) {
			return 0L;
		}

		NBTTagCompound timeData = stack.getSubCompound(LEGACY_NBT_TIMEDATA);
		if (timeData == null || !timeData.hasKey(LEGACY_NBT_STORED_TIME)) {
			return 0L;
		}

		return timeData.getInteger(LEGACY_NBT_STORED_TIME) & 0xFFFFFFFFL;
	}

	public static void setLegacyStoredTime(ItemStack stack, long time)
	{
		if (stack == null || stack.isEmpty()) {
			return;
		}

		NBTTagCompound timeData = stack.getOrCreateSubCompound(LEGACY_NBT_TIMEDATA);
		timeData.setInteger(LEGACY_NBT_STORED_TIME, (int) Math.max(0L, Math.min(MAX_BOTTLED_TIME_TICKS, time)));
	}

	// Send the current bottled time to the client so tooltip updates
	public static void syncBottledTimeToClient(EntityPlayer player)
	{
		if (player == null || player.world.isRemote)
			return;
		IBottledTime cap = player.getCapability(IBottledTime.CAPABILITY_BOTTLED_TIME, null);
		if (cap != null && player instanceof EntityPlayerMP)
			PacketHandler.instance().sendTo(new MessageBottledTimeSync(cap.getBottledTime()), (EntityPlayerMP) player);
	}
}
