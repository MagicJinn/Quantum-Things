package lumien.randomthings.recipes.imbuing;

import java.util.ArrayList;

import lumien.randomthings.util.ItemUtil;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ImbuingRecipe
{
	ItemStack toImbue;
	ArrayList<ItemStack> ingredients;
	ItemStack result;
	public boolean transferNBT;

	// Preserve old behavior of not transferring NBT for compatibility
	public ImbuingRecipe(ItemStack toImbue, ItemStack result, ItemStack... ingredients)
	{
		this(toImbue, result, false, ingredients);
	}

	public ImbuingRecipe(ItemStack toImbue, ItemStack result, boolean transferNBT, ItemStack... ingredients) {
		this.toImbue = toImbue;
		this.ingredients = new ArrayList<>();
		this.result = result;
		this.transferNBT = transferNBT;
		for (ItemStack is : ingredients)
		{
			if (!is.isEmpty())
				this.ingredients.add(is);
		}
	}

	public boolean matchesItemHandler(IItemHandler iItemHandler)
	{
		ItemStack i1 = iItemHandler.getStackInSlot(0);
		ItemStack i2 = iItemHandler.getStackInSlot(1);
		ItemStack i3 = iItemHandler.getStackInSlot(2);
		ItemStack center = iItemHandler.getStackInSlot(3);

		// Track which slots have been used (0, 1, 2)
		boolean[] usedSlots = new boolean[3];
		ItemStack[] slotItems = new ItemStack[] { i1, i2, i3 };

		// Legacy behavior, not transferring NBT
		if (!transferNBT && !ItemUtil.areItemStackContentEqual(center, toImbue)
				&& !ItemUtil.areOreDictionaried(center, toImbue)) {
			return false;
		}

		// Check center item, ignore NBT to allow items with enchantments/damage/etc
		// Only check item type and damage, not NBT tags
		// For armor items, ignore damage value (durability) to allow damaged armor
		if (center.isEmpty() || toImbue.isEmpty() || center.getItem() != toImbue.getItem()) {
			return false;
		}
		// For damageable items (armor, tools), damage represents durability, so ignore
		// it. For non-damageable items, damage represents metadata, so check it.
		if (!center.getItem().isDamageable() && center.getItemDamage() != toImbue.getItemDamage())
		{
			return false;
		}

		// For each needed ingredient, find an unused matching slot
		for (ItemStack needed : ingredients)
		{
			boolean found = false;
			for (int slot = 0; slot < 3; slot++) {
				if (!usedSlots[slot] && !slotItems[slot].isEmpty()) {
					if (ItemUtil.areItemStackContentEqual(slotItems[slot], needed)
							|| ItemUtil.areOreDictionaried(slotItems[slot], needed)) {
						usedSlots[slot] = true;
						found = true;
						break;
					}
				}
			}
			if (!found)
			{
				return false;
			}
		}

		// Check that all non-empty slots have been used
		for (int slot = 0; slot < 3; slot++)
		{
			if (!slotItems[slot].isEmpty() && !usedSlots[slot])
			{
				return false;
			}
		}

		return true;
	}

	public boolean containsAsIngredient(ItemStack is)
	{
		if (ItemUtil.areItemStackContentEqual(toImbue, is) || ItemUtil.areOreDictionaried(toImbue, is))
		{
			return true;
		}

		for (ItemStack ingredient : ingredients)
		{
			if (ItemUtil.areItemStackContentEqual(ingredient, is) || ItemUtil.areOreDictionaried(ingredient, is))
			{
				return true;
			}
		}

		return false;
	}

	public ArrayList<ItemStack> getIngredients()
	{
		return ingredients;
	}

	public ItemStack toImbue()
	{
		return toImbue;
	}

	public ItemStack getResult()
	{
		return result;
	}
}
