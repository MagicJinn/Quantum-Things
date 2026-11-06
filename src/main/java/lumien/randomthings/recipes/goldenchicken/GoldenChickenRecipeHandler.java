package lumien.randomthings.recipes.goldenchicken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class GoldenChickenRecipeHandler
{
	public static final int DEFAULT_MIN_DIGEST_TIME = 600;
	public static final int DEFAULT_MAX_DIGEST_TIME = 1200;

	private static final List<GoldenChickenRecipe> RECIPES = new ArrayList<>();

	public static void registerDefaults()
	{
		addRecipe(GoldenChickenRecipe.oreDict("oreGold", new ItemStack(Items.GOLD_INGOT, 3), DEFAULT_MIN_DIGEST_TIME, DEFAULT_MAX_DIGEST_TIME));
	}

	public static void addRecipe(GoldenChickenRecipe recipe)
	{
		RECIPES.add(recipe);
	}

	public static void removeAll()
	{
		RECIPES.clear();
	}

	public static void removeIf(Predicate<GoldenChickenRecipe> predicate)
	{
		RECIPES.removeIf(predicate);
	}

	public static GoldenChickenRecipe findRecipe(ItemStack stack)
	{
		for (GoldenChickenRecipe recipe : RECIPES)
		{
			if (recipe.matches(stack))
			{
				return recipe;
			}
		}
		return null;
	}

	public static List<GoldenChickenRecipe> getRecipes()
	{
		return Collections.unmodifiableList(RECIPES);
	}
}
