package lumien.randomthings.item;

import java.util.List;
import java.util.Optional;

import lumien.randomthings.config.Numbers;
import lumien.randomthings.entitys.EntityTimeAccelerator;
import lumien.randomthings.capability.bottledtime.IBottledTime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import lumien.randomthings.network.PacketHandler;
import lumien.randomthings.network.client.MessageBottledTimeSync;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemTimeInABottle extends ItemBase
{
	// Increased limit (68 years) but still safe enough to prevent overflow
	private static final long MAX_BOTTLED_TIME_TICKS = 42949672940L;

	public ItemTimeInABottle()
	{
		super("timeInABottle");

		this.setMaxStackSize(1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		super.addInformation(stack, worldIn, tooltip, flagIn);

		EntityPlayer player = Minecraft.getMinecraft().player;
		long storedTime = 0;
		if (player != null) {
			IBottledTime cap = player.getCapability(IBottledTime.CAPABILITY_BOTTLED_TIME, null);
			if (cap != null)
				storedTime = cap.getBottledTime();
		}

		int storedSeconds = (int) (storedTime / 20);

		int hours = storedSeconds / 3600;
		int minutes = (storedSeconds % 3600) / 60;
		int seconds = storedSeconds % 60;

		tooltip.add(I18n.format("tooltip.timeInABottle", hours, minutes, seconds));
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
			IBottledTime cap = player.getCapability(IBottledTime.CAPABILITY_BOTTLED_TIME, null);
			if (cap == null)
				return;

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

					IBottledTime cap = player.getCapability(IBottledTime.CAPABILITY_BOTTLED_TIME, null);
					long timeAvailable = cap != null ? cap.getBottledTime() : 0;

					if (timeAvailable >= timeRequired || player.capabilities.isCreativeMode)
					{
						int timeAdded = (nextRate * usedUpTime - currentRate * usedUpTime) / nextRate;

						if (!player.capabilities.isCreativeMode && cap != null) {
							cap.setBottledTime(timeAvailable - timeRequired);
							syncBottledTimeToClient(player);
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
				IBottledTime cap = player.getCapability(IBottledTime.CAPABILITY_BOTTLED_TIME, null);
				long timeAvailable = cap != null ? cap.getBottledTime() : 0;

				if (timeAvailable >= 20 * 30 || player.capabilities.isCreativeMode)
				{
					if (!player.capabilities.isCreativeMode && cap != null) {
						cap.setBottledTime(timeAvailable - 20 * 30);
						syncBottledTimeToClient(player);
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
		IBottledTime cap = player != null ? player.getCapability(IBottledTime.CAPABILITY_BOTTLED_TIME, null) : null;
		return cap != null ? cap.getBottledTime() : 0;
	}

	// Set the stored time in the player's capability
	public static void setStoredTime(EntityPlayer player, long time) {
		IBottledTime cap = player != null ? player.getCapability(IBottledTime.CAPABILITY_BOTTLED_TIME, null) : null;
		if (cap != null) {
			cap.setBottledTime(time);
			syncBottledTimeToClient(player);
		}
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
