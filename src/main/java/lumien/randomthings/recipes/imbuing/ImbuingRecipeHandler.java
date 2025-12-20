package lumien.randomthings.recipes.imbuing;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ImbuingRecipeHandler
{
	public static ArrayList<ImbuingRecipe> imbuingRecipes = new ArrayList<>();

	public static ItemStack getRecipeOutput(IItemHandler iItemHandler)
	{
		for (ImbuingRecipe ir : imbuingRecipes)
		{
			if (ir.matchesItemHandler(iItemHandler))
			{
				return ir.result;
			}
		}
		return ItemStack.EMPTY;
	}

	public static ImbuingRecipe getMatchingRecipe(IItemHandler iItemHandler) {
		for (ImbuingRecipe ir : imbuingRecipes) {
			if (ir.matchesItemHandler(iItemHandler)) {
				return ir;
			}
		}
		return null;
	}

	// Preserve old behavior of not transferring NBT for compatibility
	public static void addRecipe(ItemStack ingredient1, ItemStack ingredient2, ItemStack ingredient3, ItemStack toImbue, ItemStack result)
	{
		addRecipe(ingredient1, ingredient2, ingredient3, toImbue, result, false);
	}

	public static void addRecipe(ItemStack ingredient1, ItemStack ingredient2, ItemStack ingredient3, ItemStack toImbue,
			ItemStack result, boolean transferNBT) {
		ImbuingRecipe toAdd = new ImbuingRecipe(toImbue, result, transferNBT, ingredient1, ingredient2, ingredient3);
		imbuingRecipes.add(toAdd);
	}
}
