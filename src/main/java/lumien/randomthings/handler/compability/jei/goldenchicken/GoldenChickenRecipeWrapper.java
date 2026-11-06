package lumien.randomthings.handler.compability.jei.goldenchicken;

import java.util.Collections;

import lumien.randomthings.recipes.goldenchicken.GoldenChickenRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class GoldenChickenRecipeWrapper extends BlankRecipeWrapper
{
	private final GoldenChickenRecipe recipe;

	public GoldenChickenRecipeWrapper(GoldenChickenRecipe recipe)
	{
		this.recipe = recipe;
	}

	@Override
	public void getIngredients(IIngredients ingredients)
	{
		ingredients.setInputLists(ItemStack.class, Collections.singletonList(recipe.getInputExamples()));
		ingredients.setOutput(ItemStack.class, recipe.getOutput());
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
	{
		int minSec = recipe.getMinDigestTime() / 20;
		int maxSec = recipe.getMaxDigestTime() / 20;
		String text;
		if (minSec == maxSec)
		{
			text = I18n.format("jei.randomthings.goldenchicken.time.exact", minSec);
		}
		else
		{
			text = I18n.format("jei.randomthings.goldenchicken.time", minSec, maxSec);
		}
		int width = minecraft.fontRenderer.getStringWidth(text);
		minecraft.fontRenderer.drawString(text, (recipeWidth - width) / 2, 32, 0xFF808080);
	}
}
