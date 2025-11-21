package lumien.randomthings.config;

import java.lang.reflect.Field;
import java.util.Set;

import org.apache.logging.log4j.Level;

import lumien.randomthings.RandomThings;
import lumien.randomthings.lib.ConfigOption;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ModConfiguration
{
	Configuration configuration;

	public Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * Ensures all config properties are registered in the Configuration object. This should be
	 * called before creating the config GUI to ensure all properties exist.
	 */
	public void ensurePropertiesRegistered() {
		if (configuration != null) {
			// This will register all properties by calling configuration.get() for each annotated
			// field
			doAnnoations(configuration);

			// Ensure divining rods list property is registered
			configuration.getStringList("Divining Rods", "Divining Rods", DiviningRods.DEFAULT_RODS,
					DiviningRods.PROPERTY_COMMENT);
			markDiviningRodsRequiresRestart();
			markSpectreDimensionRequiresRestart();
		}
	}

	public void preInit(FMLPreInitializationEvent event)
	{
		configuration = new Configuration(event.getSuggestedConfigurationFile());

		// Force load NatureCore class to ensure it's included in ASM scanning
		NatureCore.class.getName();
		DiviningRods.class.getName();
		SpectreCoils.class.getName();

		// Load and process configuration
		reloadConfig();

		// Register divining rods list property to ensure it's in the config file on startup
		// This must be done here so the property is registered before the config is saved
		configuration.getStringList("Divining Rods", "Divining Rods", DiviningRods.DEFAULT_RODS,
				DiviningRods.PROPERTY_COMMENT);
		markDiviningRodsRequiresRestart();
		markSpectreDimensionRequiresRestart();

		// Set category comments after loading (only needed on initial setup)
		if (configuration.hasCategory("Worldgen Plants")) {
			configuration.getCategory("Worldgen Plants").setComment(
					"Enable or disable generation of plants, or change their frequency");
		}
		if (configuration.hasCategory("Worldgen Features")) {
			configuration.getCategory("Worldgen Features").setComment(
					"Enable or disable generation of structures and special blocks, or change their frequency");
		}
		if (configuration.hasCategory("Worldgen Loot")) {
			configuration.getCategory("Worldgen Loot").setComment(
					"Enable or disable generation of loot items in chests and structures, or change their frequency");
		}
		if (configuration.hasCategory("Nature Core")) {
			configuration.getCategory("Nature Core").setComment("Configure Nature Core behavior");
		}

		if (configuration.hasCategory("Lotus")) {
			configuration.getCategory("Lotus").setComment("Configure Lotus plant behavior");
		}

		if (configuration.hasCategory("Divining Rods")) {
			configuration.getCategory("Divining Rods").setComment(DiviningRods.CONFIG_COMMENT);
		}

		if (configuration.hasCategory("Spectre Coils")) {
			configuration.getCategory("Spectre Coils")
					.setComment("Configure Spectre Coils and Chargers behavior");
		}

		if (configuration.hasChanged()) {
			configuration.save();
		}
	}

	public void reloadConfig() {
		if (configuration != null) {
			// Load the configuration from disk
			configuration.load();

			// Reload annotation-based config - this reads from config and sets static fields
			doAnnoations(configuration);

			// Validate worldgen chances
			checkSettingsValid();
		}
	}

	/**
	 * Syncs static fields from the Configuration object without reloading from disk. Use this after
	 * the GUI saves changes, as the Configuration already has the updated values.
	 */
	public void syncStaticFields() {
		if (configuration != null) {
			// Sync annotation-based config - this reads from the Configuration object and sets
			// static fields
			// without calling configuration.load(), since the Configuration already has the updated
			// values
			doAnnoations(configuration);

			// Validate worldgen chances
			checkSettingsValid();
		}
	}

	private void doAnnoations(Configuration configuration)
	{
		ASMDataTable asmData = RandomThings.instance.getASMData();

		if (asmData == null) {
			RandomThings.logger.log(Level.WARN,
					"ASMDataTable is null, cannot load config annotations");
			return;
		}

		Set<ASMData> atlasSet = asmData.getAll(ConfigOption.class.getName());

		for (ASMData data : atlasSet)
		{
			try
			{
				@SuppressWarnings("rawtypes")
				Class clazz = Class.forName(data.getClassName());
				Field f = clazz.getDeclaredField(data.getObjectName());
				f.setAccessible(true);

				String name = (String) data.getAnnotationInfo().get("name");
				String category = (String) data.getAnnotationInfo().get("category");
				String comment = (String) data.getAnnotationInfo().get("comment");

				if (comment == null) {
					comment = "";
				}

				Object result = null;
				Object defaultValue = null;

				// Get the default value from the static field
				// Get or create the Property object - this ensures we're using the same Property
				// that the GUI edits
				Property prop = null;
				if (f.getType() == boolean.class)
				{
					defaultValue = f.getBoolean(null);
					prop = configuration.get(category, name, (Boolean) defaultValue, comment);
					result = prop.getBoolean();
				}
				else if (f.getType() == double.class)
				{
					defaultValue = f.getDouble(null);
					prop = configuration.get(category, name, (Double) defaultValue, comment);
					result = prop.getDouble();
				}
				else if (f.getType() == int.class)
				{
					defaultValue = f.getInt(null);
					prop = configuration.get(category, name, (Integer) defaultValue, comment);
					result = prop.getInt();
				} else {
					RandomThings.logger.log(Level.ERROR,
							"Invalid Data Type for Config annotation: " + f.getType()
									+ " for field " + clazz.getName() + "." + data.getObjectName());
					continue;
				}

				// Set the static field with the value from config Property
				// This reads the CURRENT value from the Property object, which may have been
				// updated by the GUI
				if (result != null)
				{
					f.set(null, result);
				}
			}
			catch (Exception e)
			{
				RandomThings.logger.log(Level.ERROR, "Error loading config option: "
						+ data.getClassName() + "." + data.getObjectName());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Marks the divining rods configuration property as requiring a Minecraft restart. This is
	 * necessary because divining rods affect item registration and recipes.
	 */
	private void markDiviningRodsRequiresRestart() {
		if (configuration != null && configuration.hasCategory("Divining Rods")) {
			Property diviningRodsProp =
					configuration.getCategory("Divining Rods").get("Divining Rods");
			if (diviningRodsProp != null) {
				diviningRodsProp.setRequiresMcRestart(true);
			}
		}
	}

	/**
	 * Marks the Spectre Dimension configuration property as requiring a Minecraft restart. This is
	 * necessary because the dimension affects world registration and initialization.
	 */
	private void markSpectreDimensionRequiresRestart() {
		if (configuration != null && configuration.hasCategory("Features")) {
			Property spectreDimensionProp =
					configuration.getCategory("Features").get("SpectreDimension");
			if (spectreDimensionProp != null) {
				spectreDimensionProp.setRequiresMcRestart(true);
			}
		}
	}

	private void checkSettingsValid() {
		// Plants
		if (Worldgen.BEANS_CHANCE <= 0)
			Worldgen.BEANS = false;
		if (Worldgen.PITCHER_PLANTS_CHANCE <= 0)
			Worldgen.PITCHER_PLANTS = false;
		if (Worldgen.LOTUS_CHANCE <= 0)
			Worldgen.LOTUS = false;
		if (Worldgen.GLOWING_MUSHROOM_CHANCE <= 0)
			Worldgen.GLOWING_MUSHROOM = false;

		// Features
		if (Worldgen.NATURE_CORE_CHANCE <= 0)
			Worldgen.NATURE_CORE = false;
		if (Worldgen.WATER_CHEST_CHANCE <= 0)
			Worldgen.WATER_CHEST = false;
		if (Worldgen.PEACE_CANDLE_CHANCE <= 0)
			Worldgen.PEACE_CANDLE = false;
		if (Worldgen.ANCIENT_FURNACE_CHANCE <= 0)
			Worldgen.ANCIENT_FURNACE = false;

		// Loot
		if (Worldgen.MAGIC_HOOD_CHANCE <= 0)
			Worldgen.MAGIC_HOOD = false;
		if (Worldgen.SUMMONING_PENDULUM_CHANCE <= 0)
			Worldgen.SUMMONING_PENDULUM = false;
		if (Worldgen.BIOME_CRYSTAL_CHANCE <= 0)
			Worldgen.BIOME_CRYSTAL = false;
		if (Worldgen.LAVA_CHARM_CHANCE <= 0)
			Worldgen.LAVA_CHARM = false;
		if (Worldgen.SLIME_CUBE_CHANCE <= 0)
			Worldgen.SLIME_CUBE = false;
		if (Worldgen.NUMBERED_COILS_CHANCE <= 0)
			Worldgen.NUMBERED_COILS = false;

		// Clamp NatureCore range values used in rand.nextInt() to minimum 1
		// These are used for range calculations, not chance, so clamp instead of disable
		if (NatureCore.SAND_RANGE <= 0)
			NatureCore.SAND_RANGE = 1;
		if (NatureCore.ANIMAL_RANGE <= 0)
			NatureCore.ANIMAL_RANGE = 1;
		if (NatureCore.BONEMEAL_RANGE <= 0)
			NatureCore.BONEMEAL_RANGE = 1;
		if (NatureCore.TREE_RADIUS_RANGE <= 0)
			NatureCore.TREE_RADIUS_RANGE = 1;
		if (NatureCore.ANIMAL_MAX < 0)
			NatureCore.ANIMAL_MAX = 0;

		// Clamp NatureCore chance values used in rand.nextInt() to minimum 1
		// These are used directly in nextInt(), so must be at least 1
		if (NatureCore.SAND_REPLACEMENT_CHANCE <= 0)
			NatureCore.SAND_REPLACEMENT_CHANCE = 1;
		if (NatureCore.ANIMAL_CHANCE <= 0)
			NatureCore.ANIMAL_CHANCE = 1;
		if (NatureCore.BONEMEAL_CHANCE <= 0)
			NatureCore.BONEMEAL_CHANCE = 1;
		if (NatureCore.TREE_CHANCE <= 0)
			NatureCore.TREE_CHANCE = 1;
		if (NatureCore.SHELL_REGENERATION_CHANCE <= 0)
			NatureCore.SHELL_REGENERATION_CHANCE = 1;
	}
}
