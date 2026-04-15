package lumien.randomthings.recipes;

import java.util.function.BooleanSupplier;

import com.google.gson.JsonObject;

import lumien.randomthings.config.Features;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

public class RecipeConditionSpectreToolsEnabled
{
	public static class Factory implements IConditionFactory
	{
		@Override
		public BooleanSupplier parse(JsonContext context, JsonObject json)
		{
			return () -> !Features.DISABLE_SPECTRE_TOOLS;
		}
	}
}
