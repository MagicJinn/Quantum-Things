package lumien.randomthings.handler.compability.jei.goldenchicken;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;

import lumien.randomthings.handler.compability.jei.RandomThingsPlugin;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class GoldenChickenRecipeCategory implements IRecipeCategory
{
	private static final int inputSlot = 0;
	private static final int outputSlot = 1;

	@Nonnull
	private final IDrawable background;

	@Nonnull
	private final IDrawable slotDrawable;

	@Nonnull
	private final IDrawable arrow;

	@Nonnull
	private final String localizedName;

	public GoldenChickenRecipeCategory(IGuiHelper guiHelper)
	{
		background = guiHelper.createBlankDrawable(116, 44);
		slotDrawable = guiHelper.getSlotDrawable();
		arrow = guiHelper.drawableBuilder(new ResourceLocation("jei", "textures/gui/gui_vanilla.png"), 82, 128, 24, 17).build();
		localizedName = Translator.translateToLocal("jei.randomthings.goldenchicken");
	}

	@Nonnull
	@Override
	public String getUid()
	{
		return RandomThingsPlugin.GOLDEN_CHICKEN_ID;
	}

	@Nonnull
	@Override
	public String getTitle()
	{
		return localizedName;
	}

	@Nonnull
	@Override
	public IDrawable getBackground()
	{
		return background;
	}

	@Override
	public void drawExtras(@Nonnull Minecraft minecraft)
	{
		slotDrawable.draw(minecraft, 16, 8);
		slotDrawable.draw(minecraft, 82, 8);
		arrow.draw(minecraft, 46, 9);
	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull IRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients)
	{
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		itemStacks.init(inputSlot, true, 16, 8);
		itemStacks.init(outputSlot, false, 82, 8);
		itemStacks.set(ingredients);
	}

	@Override
	public IDrawable getIcon()
	{
		return null;
	}

	@Override
	public List getTooltipStrings(int mouseX, int mouseY)
	{
		return Lists.newArrayList();
	}

	@Override
	public String getModName()
	{
		return "Quantum Things";
	}
}
