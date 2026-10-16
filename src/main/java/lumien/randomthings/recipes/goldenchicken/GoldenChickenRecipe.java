package lumien.randomthings.recipes.goldenchicken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class GoldenChickenRecipe
{
	private final String oreName;
	private final List<ItemStack> items;
	private final Predicate<ItemStack> extraMatcher;
	private final ItemStack output;
	private final int minDigestTime;
	private final int maxDigestTime;

	private GoldenChickenRecipe(String oreName, List<ItemStack> items, Predicate<ItemStack> extraMatcher, ItemStack output, int minDigestTime, int maxDigestTime)
	{
		this.oreName = oreName;
		this.items = new ArrayList<>();
		if (items != null)
		{
			for (ItemStack stack : items)
			{
				if (stack != null && !stack.isEmpty())
				{
					this.items.add(stack.copy());
				}
			}
		}
		this.extraMatcher = extraMatcher;
		this.output = output.copy();
		this.minDigestTime = Math.max(1, minDigestTime);
		this.maxDigestTime = Math.max(this.minDigestTime, maxDigestTime);
	}

	public static GoldenChickenRecipe oreDict(String oreName, ItemStack output, int minDigestTime, int maxDigestTime)
	{
		return new GoldenChickenRecipe(oreName, Collections.emptyList(), null, output, minDigestTime, maxDigestTime);
	}

	public static GoldenChickenRecipe items(List<ItemStack> inputs, ItemStack output, int minDigestTime, int maxDigestTime)
	{
		return new GoldenChickenRecipe(null, inputs, null, output, minDigestTime, maxDigestTime);
	}

	public static GoldenChickenRecipe matching(Predicate<ItemStack> matcher, List<ItemStack> examples, ItemStack output, int minDigestTime, int maxDigestTime)
	{
		return new GoldenChickenRecipe(null, examples, matcher, output, minDigestTime, maxDigestTime);
	}

	public boolean matches(ItemStack stack)
	{
		if (stack == null || stack.isEmpty())
		{
			return false;
		}
		if (extraMatcher != null)
		{
			return extraMatcher.test(stack);
		}
		if (oreName != null && !oreName.isEmpty() && OreDictionary.doesOreNameExist(oreName))
		{
			int id = OreDictionary.getOreID(oreName);
			for (int oreId : OreDictionary.getOreIDs(stack))
			{
				if (oreId == id)
				{
					return true;
				}
			}
			return false;
		}
		for (ItemStack input : items)
		{
			if (OreDictionary.itemMatches(input, stack, false))
			{
				return true;
			}
		}
		return false;
	}

	public int rollDigestTime(Random rand)
	{
		if (maxDigestTime <= minDigestTime)
		{
			return minDigestTime;
		}
		return minDigestTime + rand.nextInt(maxDigestTime - minDigestTime);
	}

	public ItemStack getOutput()
	{
		return output.copy();
	}

	public String getOreName()
	{
		return oreName;
	}

	public int getMinDigestTime()
	{
		return minDigestTime;
	}

	public int getMaxDigestTime()
	{
		return maxDigestTime;
	}

	public List<ItemStack> getInputExamples()
	{
		List<ItemStack> list = new ArrayList<>();
		if (oreName != null && !oreName.isEmpty() && OreDictionary.doesOreNameExist(oreName))
		{
			for (ItemStack stack : OreDictionary.getOres(oreName))
			{
				list.add(stack.copy());
			}
			if (!list.isEmpty())
			{
				return list;
			}
		}
		for (ItemStack stack : items)
		{
			list.add(stack.copy());
		}
		return list;
	}
}
