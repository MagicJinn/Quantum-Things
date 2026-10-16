package lumien.randomthings.handler.compability.crafttweaker;

import java.util.ArrayList;
import java.util.List;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.oredict.IOreDictEntry;
import lumien.randomthings.recipes.goldenchicken.GoldenChickenRecipe;
import lumien.randomthings.recipes.goldenchicken.GoldenChickenRecipeHandler;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.randomthings.GoldenChicken")
public class GoldenChickenTweaker
{
	@ZenMethod
	public static void addRecipe(IIngredient input, IItemStack output)
	{
		addRecipe(input, output, GoldenChickenRecipeHandler.DEFAULT_MIN_DIGEST_TIME, GoldenChickenRecipeHandler.DEFAULT_MAX_DIGEST_TIME);
	}

	@ZenMethod
	public static void addRecipe(IIngredient input, IItemStack output, int digestTime)
	{
		addRecipe(input, output, digestTime, digestTime);
	}

	@ZenMethod
	public static void addRecipe(IIngredient input, IItemStack output, int minDigestTime, int maxDigestTime)
	{
		if (input == null)
		{
			CraftTweakerAPI.logError("Golden Chicken recipe input cannot be null");
			return;
		}
		if (output == null)
		{
			CraftTweakerAPI.logError("Golden Chicken recipe output cannot be null");
			return;
		}

		ItemStack outputStack = CraftTweakerMC.getItemStack(output);
		if (outputStack.isEmpty())
		{
			CraftTweakerAPI.logError("Golden Chicken recipe output cannot be empty");
			return;
		}

		CraftTweakerAPI.apply(new Add(input, outputStack, minDigestTime, maxDigestTime));
	}

	@ZenMethod
	public static void removeRecipe(IIngredient input)
	{
		if (input == null)
		{
			CraftTweakerAPI.logError("Golden Chicken removeRecipe input cannot be null");
			return;
		}
		CraftTweakerAPI.apply(new Remove(input));
	}

	@ZenMethod
	public static void removeAll()
	{
		CraftTweakerAPI.apply(new RemoveAll());
	}

	private static List<ItemStack> toItemStacks(IIngredient input)
	{
		List<ItemStack> stacks = new ArrayList<>();
		if (input.getItems() == null)
		{
			return stacks;
		}
		for (IItemStack item : input.getItems())
		{
			ItemStack stack = CraftTweakerMC.getItemStack(item);
			if (!stack.isEmpty())
			{
				stacks.add(stack);
			}
		}
		return stacks;
	}

	private static class Add implements IAction
	{
		private final IIngredient input;
		private final ItemStack output;
		private final int minDigestTime;
		private final int maxDigestTime;

		private Add(IIngredient input, ItemStack output, int minDigestTime, int maxDigestTime)
		{
			this.input = input;
			this.output = output;
			this.minDigestTime = minDigestTime;
			this.maxDigestTime = maxDigestTime;
		}

		@Override
		public void apply()
		{
			GoldenChickenRecipe recipe;
			if (input instanceof IOreDictEntry)
			{
				recipe = GoldenChickenRecipe.oreDict(((IOreDictEntry) input).getName(), output, minDigestTime, maxDigestTime);
			}
			else
			{
				recipe = GoldenChickenRecipe.matching(stack -> {
					IItemStack ctStack = CraftTweakerMC.getIItemStack(stack);
					return ctStack != null && input.matches(ctStack);
				}, toItemStacks(input), output, minDigestTime, maxDigestTime);
			}
			GoldenChickenRecipeHandler.addRecipe(recipe);
		}

		@Override
		public String describe()
		{
			return "Adding Golden Chicken recipe for " + input + " -> " + output;
		}
	}

	private static class Remove implements IAction
	{
		private final IIngredient input;

		private Remove(IIngredient input)
		{
			this.input = input;
		}

		@Override
		public void apply()
		{
			GoldenChickenRecipeHandler.removeIf(recipe -> {
				if (input instanceof IOreDictEntry && recipe.getOreName() != null)
				{
					if (recipe.getOreName().equals(((IOreDictEntry) input).getName()))
					{
						return true;
					}
				}
				for (ItemStack example : recipe.getInputExamples())
				{
					IItemStack ctStack = CraftTweakerMC.getIItemStack(example);
					if (ctStack != null && input.matches(ctStack))
					{
						return true;
					}
				}
				return false;
			});
		}

		@Override
		public String describe()
		{
			return "Removing Golden Chicken recipes matching " + input;
		}
	}

	private static class RemoveAll implements IAction
	{
		@Override
		public void apply()
		{
			GoldenChickenRecipeHandler.removeAll();
		}

		@Override
		public String describe()
		{
			return "Removing all Golden Chicken recipes";
		}
	}
}
