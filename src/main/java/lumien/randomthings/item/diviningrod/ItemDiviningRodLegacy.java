package lumien.randomthings.item.diviningrod;

import java.awt.Color;
import java.util.List;

import lumien.randomthings.RandomThings;
import lumien.randomthings.item.ItemBase;
import lumien.randomthings.lib.IRTItemColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

/**
 * Legacy divining rod item that uses metadata variants.
 * This is kept for migration purposes
 * Old items will be converted to new individual items.
 */
public class ItemDiviningRodLegacy extends ItemBase implements IRTItemColor {
	public ItemDiviningRodLegacy() {
		// Use the old registry name so old worlds can load it
		super("diviningRod");
		this.setHasSubtypes(true);
		this.setMaxStackSize(1);
		// Hide from creative tab - this item is only for migration
		this.setCreativeTab(null);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		// Don't show in creative menu
		// The item still supports all metadata values for loading old worlds
	}

	@Override
	public String getTranslationKey(ItemStack stack) {
		return "item.diviningRodLegacy";
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		// Show legacy name, but also show what type it is
		String legacyName = I18n.translateToLocal("item.diviningRodLegacy.name");
		int meta = stack.getItemDamage();
		if (meta >= 0 && meta < ItemDiviningRod.types.size()) {
			RodType type = ItemDiviningRod.types.get(meta);
			if (type != null) {
				// Try to get the display name from the new item
				ItemDiviningRod newItem = ItemDiviningRod.rodItems.get(type);
				if (newItem != null) {
					ItemStack newStack = new ItemStack(newItem, 1);
					String newName = newItem.getItemStackDisplayName(newStack);
					return legacyName + " (" + newName + ")";
				}
			}
		}
		return legacyName;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip,
			ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add(I18n.translateToLocal("item.diviningRodLegacy.migration"));
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		ItemStack stack = playerIn.getHeldItem(handIn);

		if (worldIn.isRemote)
			return ActionResult.newResult(net.minecraft.util.EnumActionResult.SUCCESS, stack);

		// Convert the legacy rod to the new item
		ItemStack converted = convertLegacyRod(stack);

		if (converted != stack) {
			// Successfully converted
			playerIn.setHeldItem(handIn, converted);
			return ActionResult.newResult(net.minecraft.util.EnumActionResult.SUCCESS, converted);
		}

		// Failed to convert
		playerIn.sendMessage(new TextComponentTranslation("item.diviningRodLegacy.convertFailed"));
		return ActionResult.newResult(net.minecraft.util.EnumActionResult.FAIL, stack);
	}

	/**
	 * Converts a legacy divining rod ItemStack to the new individual item format.
	 * This is a static method so it can be used by both the item itself and
	 * migration handlers.
	 * 
	 * @param oldStack   The legacy divining rod ItemStack to convert
	 * @param logContext Optional context string for logging (e.g., "Player", "World
	 *                   load")
	 * @return The converted ItemStack, or the original if conversion failed
	 */
	public static ItemStack convertLegacyRod(ItemStack oldStack, String logContext) {
		// Fail states
		if (oldStack.isEmpty() || !(oldStack.getItem() instanceof ItemDiviningRodLegacy)) {
			return oldStack;
		}

		int metadata = oldStack.getItemDamage();
		if (metadata < 0 || metadata >= ItemDiviningRod.types.size()) {
			RandomThings.logger.log(Level.WARN,
					"Invalid metadata " + metadata + " for legacy divining rod, cannot convert");
			return oldStack;
		}

		RodType type = ItemDiviningRod.types.get(metadata);
		if (type == null) {
			RandomThings.logger.log(Level.WARN, "Rod type at index " + metadata + " is null, cannot convert");
			return oldStack;
		}

		ItemDiviningRod newItem = ItemDiviningRod.rodItems.get(type);
		if (newItem == null) {
			RandomThings.logger.log(Level.WARN, "Could not find new item for rod type: " + type.getName());
			return oldStack;
		}

		// Create new ItemStack with the new item
		ItemStack newStack = new ItemStack(newItem, oldStack.getCount());

		// Copy NBT data if present
		NBTTagCompound oldNBT = oldStack.getTagCompound();
		if (oldNBT != null) {
			newStack.setTagCompound(oldNBT.copy());
		}

		String context = logContext != null && !logContext.isEmpty() ? logContext + " " : "";
		RandomThings.logger.log(Level.INFO, context + "converted legacy rod (metadata " + metadata + ", type "
				+ type.getName() + ") to new item: " + newItem.getRegistryName());
		return newStack;
	}

	private ItemStack convertLegacyRod(ItemStack oldStack) {
		return convertLegacyRod(oldStack, "Player");
	}

	@Override
	public int getColorFromItemstack(ItemStack stack, int tintIndex) {
		int meta = stack.getItemDamage();

		if (tintIndex == 1 && meta < ItemDiviningRod.types.size()) {
			return ItemDiviningRod.types.get(meta).getItemColor().getRGB();
		} else {
			return Color.WHITE.getRGB();
		}
	}
}
