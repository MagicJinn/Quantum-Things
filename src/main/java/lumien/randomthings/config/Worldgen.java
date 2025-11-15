package lumien.randomthings.config;

import lumien.randomthings.lib.ConfigOption;

public class Worldgen
{
	private static final String CATEGORY_PLANTS = "worldgen-plants";
	private static final String CATEGORY_FEATURES = "worldgen-features";
	private static final String CATEGORY_LOOT = "worldgen-loot";

	// Worldgen - Plants and natural generation
	@ConfigOption(category = CATEGORY_PLANTS, name = "Beans",
			comment = "Enable beans generation") public static boolean beans = true;

	@ConfigOption(category = CATEGORY_PLANTS, name = "BeansChance",
			comment = "Chance for beans generation (Default = 2, lower is more common)") public static int BEANS_CHANCE =
					2;

	@ConfigOption(category = CATEGORY_PLANTS, name = "PitcherPlants",
			comment = "Enable pitcher plants generation") public static boolean pitcherPlants =
					true;

	@ConfigOption(category = CATEGORY_PLANTS, name = "PitcherPlantsChance",
			comment = "Chance for pitcher plants generation (Default = 10, lower is more common)") public static int PITCHER_PLANTS_CHANCE =
					10;

	@ConfigOption(category = CATEGORY_PLANTS, name = "Lotus",
			comment = "Enable lotus generation") public static boolean LOTUS = true;

	@ConfigOption(category = CATEGORY_PLANTS, name = "LotusChance",
			comment = "Chance for lotus generation (Default = 10, lower is more common)") public static int LOTUS_CHANCE =
					10;

	@ConfigOption(category = CATEGORY_PLANTS, name = "Sakanade",
			comment = "Enable sakanade generation") public static boolean sakanade = true;

	@ConfigOption(category = CATEGORY_PLANTS, name = "GlowingMushrooms",
			comment = "Enable glowing mushrooms generation") public static boolean GLOWING_MUSHROOM =
					true;

	@ConfigOption(category = CATEGORY_PLANTS, name = "GlowingMushroomsChance",
			comment = "Chance for glowing mushrooms generation (Default = 4, lower is more common)") public static int GLOWING_MUSHROOM_CHANCE =
					4;

	// Features - Structures and special blocks
	@ConfigOption(category = CATEGORY_FEATURES, name = "Nature-Core",
			comment = "Enable nature core generation") public static boolean natureCore = true;

	@ConfigOption(category = CATEGORY_FEATURES, name = "NatureCoreChance",
			comment = "Chance for nature core generation (Default = 18, lower is more common)") public static int NATURE_CORE_CHANCE =
					18;

	@ConfigOption(category = CATEGORY_FEATURES, name = "WaterChest",
			comment = "Enable water chest generation") public static boolean WATER_CHEST = true;

	@ConfigOption(category = CATEGORY_FEATURES, name = "PeaceCandle",
			comment = "Enable peace candle generation") public static boolean PEACE_CANDLE = true;

	@ConfigOption(category = CATEGORY_FEATURES, name = "AncientFurnace",
			comment = "Enable ancient furnace generation") public static boolean ANCIENT_FURNACE =
					true;

	// Loot - Items found in chests and structures
	@ConfigOption(category = CATEGORY_LOOT, name = "MagicHood",
			comment = "Enable magic hood loot generation")
	public static boolean MAGIC_HOOD = true;

	@ConfigOption(category = CATEGORY_LOOT, name = "SummoningPendulum",
			comment = "Enable summoning pendulum loot generation")
	public static boolean SUMMONING_PENDULUM = true;

	@ConfigOption(category = CATEGORY_LOOT, name = "BiomeCrystal",
			comment = "Enable biome crystal loot generation")
	public static boolean BIOME_CRYSTAL = true;

	@ConfigOption(category = CATEGORY_LOOT, name = "LavaCharm",
			comment = "Enable lava charm loot generation")
	public static boolean LAVA_CHARM = true;

	@ConfigOption(category = CATEGORY_LOOT, name = "SlimeCube",
			comment = "Enable slime cube loot generation")
	public static boolean SLIME_CUBE = true;
	
	@ConfigOption(category = CATEGORY_LOOT, name = "NumberedSpectreCoils",
			comment = "Enable numbered spectre coils loot generation")
	public static boolean NUMBERED_COILS = true;
}
