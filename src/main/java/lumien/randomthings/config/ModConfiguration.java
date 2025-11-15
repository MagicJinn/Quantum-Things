package lumien.randomthings.config;

import java.lang.reflect.Field;
import java.util.Set;

import org.apache.logging.log4j.Level;

import lumien.randomthings.RandomThings;
import lumien.randomthings.lib.ConfigOption;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ModConfiguration
{
	Configuration configuration;

	public void preInit(FMLPreInitializationEvent event)
	{
		configuration = new Configuration(event.getSuggestedConfigurationFile());
		configuration.load();

		Features.removeAirBubble = configuration.getBoolean("RemoveUnderwaterTexture", "Features", false, "TRIES to remove the weird water texture showing around ALL non full blocks. This might look weird when you, for example, are on a ladder underwater.");

		configuration.getCategory("worldgen-plants").setComment(
				"Enable or disable generation of plants, or change their frequency");
		configuration.getCategory("worldgen-features").setComment(
				"Enable or disable generation of structures and special blocks, or change their frequency");
		configuration.getCategory("worldgen-loot").setComment(
				"Enable or disable generation of loot items in chests and structures, or change their frequency");
		configuration.getCategory("nature-core").setComment("Configure Nature Core behavior");

		// Annotation Based Config
		doAnnoations(configuration);

		checkWorldGenChanceValid();

		if (configuration.hasChanged())
		{
			configuration.save();
		}
	}

	private void doAnnoations(Configuration configuration)
	{
		ASMDataTable asmData = RandomThings.instance.getASMData();

		Set<ASMData> atlasSet = asmData.getAll(ConfigOption.class.getName());

		for (ASMData data : atlasSet)
		{
			try
			{
				Class clazz = Class.forName(data.getClassName());
				Field f = clazz.getDeclaredField(data.getObjectName());
				f.setAccessible(true);

				String name = (String) data.getAnnotationInfo().get("name");
				String category = (String) data.getAnnotationInfo().get("category");
				String comment = (String) data.getAnnotationInfo().get("comment");

				Object result = null;

				if (f.getType() == boolean.class)
				{
					result = configuration.get(category, name, f.getBoolean(null), comment).getBoolean();
				}
				else if (f.getType() == double.class)
				{
					result = configuration.get(category, name, f.getDouble(null), comment).getDouble();
				}
				else if (f.getType() == int.class)
				{
					result = configuration.get(category, name, f.getInt(null), comment).getInt();
				}

				if (result != null)
				{
					f.set(null, result);
				}
				else
				{
					throw new RuntimeException("Invalid Data Type for Config annotation: " + f.getType());
				}
			}
			catch (Exception e)
			{
				RandomThings.instance.logger.log(Level.ERROR, "Error stitching extra textures");
				e.printStackTrace();
			}
		}

	}

	private void checkWorldGenChanceValid() {
		// Plants
		if (Worldgen.BEANS_CHANCE == 0)
			Worldgen.BEANS = false;
		if (Worldgen.PITCHER_PLANTS_CHANCE == 0)
			Worldgen.PITCHER_PLANTS = false;
		if (Worldgen.LOTUS_CHANCE == 0)
			Worldgen.LOTUS = false;
		if (Worldgen.GLOWING_MUSHROOM_CHANCE == 0)
			Worldgen.GLOWING_MUSHROOM = false;

		// Features
		if (Worldgen.NATURE_CORE_CHANCE == 0)
			Worldgen.NATURE_CORE = false;
		if (Worldgen.WATER_CHEST_CHANCE == 0)
			Worldgen.WATER_CHEST = false;
		if (Worldgen.PEACE_CANDLE_CHANCE == 0)
			Worldgen.PEACE_CANDLE = false;
		if (Worldgen.ANCIENT_FURNACE_CHANCE == 0)
			Worldgen.ANCIENT_FURNACE = false;

		// Loot
		if (Worldgen.MAGIC_HOOD_CHANCE == 0)
			Worldgen.MAGIC_HOOD = false;
		if (Worldgen.SUMMONING_PENDULUM_CHANCE == 0)
			Worldgen.SUMMONING_PENDULUM = false;
		if (Worldgen.BIOME_CRYSTAL_CHANCE == 0)
			Worldgen.BIOME_CRYSTAL = false;
		if (Worldgen.LAVA_CHARM_CHANCE == 0)
			Worldgen.LAVA_CHARM = false;
		if (Worldgen.SLIME_CUBE_CHANCE == 0)
			Worldgen.SLIME_CUBE = false;
		if (Worldgen.NUMBERED_COILS_CHANCE == 0)
			Worldgen.NUMBERED_COILS = false;
	}
}
