package lumien.randomthings.handler.compability.jei;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.client.gui.GuiImbuingStation;
import lumien.randomthings.container.ContainerImbuingStation;
import lumien.randomthings.handler.compability.jei.goldenchicken.GoldenChickenRecipeCategory;
import lumien.randomthings.handler.compability.jei.goldenchicken.GoldenChickenRecipeWrapper;
import lumien.randomthings.handler.compability.jei.imbuing.ImbuingRecipeCategory;
import lumien.randomthings.handler.compability.jei.imbuing.ImbuingRecipeWrapper;
import lumien.randomthings.item.ItemIngredient;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.recipes.goldenchicken.GoldenChickenRecipe;
import lumien.randomthings.recipes.goldenchicken.GoldenChickenRecipeHandler;
import lumien.randomthings.recipes.anvil.AnvilRecipe;
import lumien.randomthings.recipes.imbuing.ImbuingRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

@JEIPlugin
public class RandomThingsPlugin implements IModPlugin
{
	private IJeiHelpers jeiHelpers;
	public static IStackHelper stackHelper;

	public static String IMBUE_ID = "Imbuing";
	public static String GOLDEN_CHICKEN_ID = "randomthings.goldenchicken";

	@Override
	public void register(IModRegistry registry)
	{
		this.jeiHelpers = registry.getJeiHelpers();
		RandomThingsPlugin.stackHelper = jeiHelpers.getStackHelper();

		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

		registry.handleRecipes(ImbuingRecipe.class, (recipe) -> (new ImbuingRecipeWrapper(recipe.getIngredients(), recipe.toImbue(), recipe.getResult())), IMBUE_ID);
		registry.addRecipeCatalyst(new ItemStack(ModBlocks.imbuingStation), IMBUE_ID);

		IRecipeTransferRegistry recipeTransferRegistry = registry.getRecipeTransferRegistry();

		recipeTransferRegistry.addRecipeTransferHandler(ContainerImbuingStation.class, IMBUE_ID, 0, 4, 5, 36);

		registry.addRecipes(lumien.randomthings.recipes.imbuing.ImbuingRecipeHandler.imbuingRecipes, IMBUE_ID);

		registry.handleRecipes(GoldenChickenRecipe.class, GoldenChickenRecipeWrapper::new, GOLDEN_CHICKEN_ID);
		registry.addRecipeCatalyst(new ItemStack(ModItems.ingredients, 1, ItemIngredient.INGREDIENT.GOLDEN_EGG.id), GOLDEN_CHICKEN_ID);
		registry.addRecipes(GoldenChickenRecipeHandler.getRecipes(), GOLDEN_CHICKEN_ID);

		List<IRecipeWrapper> anvilRecipes = new ArrayList<IRecipeWrapper>();
		for (AnvilRecipe ar : lumien.randomthings.recipes.anvil.AnvilRecipeHandler.getAllRecipes())
		{
			anvilRecipes.add(jeiHelpers.getVanillaRecipeFactory().createAnvilRecipe(ar.getFirst(), Lists.newArrayList(ar.getSecond()), Lists.newArrayList(ar.getOutput())));
		}
		
		jeiHelpers.getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(ModBlocks.lotus));

		registry.addRecipes(anvilRecipes, VanillaRecipeCategoryUid.ANVIL);

		registry.addRecipeClickArea(GuiImbuingStation.class, 99, 54, 22, 16, IMBUE_ID);

		DescriptionHandler.addDescriptions(registry);
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry)
	{
		registry.addRecipeCategories(new ImbuingRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new GoldenChickenRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime)
	{

	}

	@Override
	public void registerItemSubtypes(@Nonnull ISubtypeRegistry subtypeRegistry)
	{
	}

	@Override
	public void registerIngredients(@Nonnull IModIngredientRegistration registry)
	{
	}
}
