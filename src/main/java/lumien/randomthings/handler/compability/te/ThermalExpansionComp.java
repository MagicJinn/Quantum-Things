package lumien.randomthings.handler.compability.te;

import java.lang.reflect.Method;

import org.apache.logging.log4j.Level;

import lumien.randomthings.RandomThings;
import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.item.ItemIngredient;
import lumien.randomthings.item.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

public class ThermalExpansionComp
{
	static final String TE_MODID = "thermalexpansion";

	public static void postInit(FMLPostInitializationEvent event)
	{
		if (Loader.isModLoaded(TE_MODID))
		{
			RandomThings.instance.logger.log(Level.INFO,
					"Adding Thermal Expansion Insolator Recipe for Spectre Saplings.");
			try
			{
				Class insolatorManager = Class.forName("cofh.thermalexpansion.util.managers.machine.InsolatorManager");
				Method addMethod = insolatorManager.getMethod("addDefaultTreeRecipe", int.class,
						ItemStack.class, ItemStack.class, ItemStack.class, int.class);

				addMethod.invoke(null, 4800, new ItemStack(ModBlocks.spectreSapling),
						new ItemStack(ModBlocks.spectreLog, 4), new ItemStack(ModItems.ingredients,
								1, ItemIngredient.INGREDIENT.ECTO_PLASM.id),
						10);
			}
			catch (Exception e)
			{
				RandomThings.instance.logger.log(Level.ERROR, "Couldn't find Insolator Recipe Handler, you won't be able to use Spectre Saplings in the Phytogenic Insolator, please report this as a bug.");
			}
		}
	}
}
