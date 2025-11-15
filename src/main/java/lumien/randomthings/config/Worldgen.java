package lumien.randomthings.config;

import lumien.randomthings.lib.ConfigOption;

public class Worldgen
{
	private static final String CATEGORY_PLANTS = "worldgen-plants";
	private static final String CATEGORY_FEATURES = "worldgen-features";
	private static final String CATEGORY_LOOT = "worldgen-loot";

	// Worldgen - Plants and natural generation
	@ConfigOption(category = CATEGORY_PLANTS, name = "Beans",
			comment = "Enable beans generation. Chance: (Default = 2, lower is more common)") public static boolean BEANS =
					true;
	@ConfigOption(category = CATEGORY_PLANTS, name = "BeansChance") public static int BEANS_CHANCE =
					2;

	@ConfigOption(category = CATEGORY_PLANTS, name = "PitcherPlants",
			comment = "Enable pitcher plants generation. Chance: (Default = 10, lower is more common)") public static boolean PITCHER_PLANTS =
					true;
	@ConfigOption(category = CATEGORY_PLANTS,
			name = "PitcherPlantsChance") public static int PITCHER_PLANTS_CHANCE =
					10;

	@ConfigOption(category = CATEGORY_PLANTS, name = "Lotus",
			comment = "Enable lotus generation. Chance: (Default = 10, lower is more common)") public static boolean LOTUS =
					true;
	@ConfigOption(category = CATEGORY_PLANTS, name = "LotusChance") public static int LOTUS_CHANCE =
					10;

	@ConfigOption(category = CATEGORY_PLANTS, name = "GlowingMushrooms",
			comment = "Enable glowing mushrooms generation. Chance: (Default = 4, lower is more common)") public static boolean GLOWING_MUSHROOM =
					true;
	@ConfigOption(category = CATEGORY_PLANTS,
			name = "GlowingMushroomsChance") public static int GLOWING_MUSHROOM_CHANCE =
					4;

	// Features - Structures and special blocks
	@ConfigOption(category = CATEGORY_FEATURES, name = "NatureCore",
			comment = "Enable nature core generation. Chance: (Default = 18, lower is more common)") public static boolean NATURE_CORE =
					true;
	@ConfigOption(category = CATEGORY_FEATURES,
			name = "NatureCoreChance") public static int NATURE_CORE_CHANCE =
					18;

	@ConfigOption(category = CATEGORY_FEATURES, name = "WaterChest",
			comment = "Enable water chest generation. Chance: (Default = 1, lower is more common)") public static boolean WATER_CHEST =
					true;
	@ConfigOption(category = CATEGORY_FEATURES,
			name = "WaterChestChance") public static int WATER_CHEST_CHANCE = 1;

	@ConfigOption(category = CATEGORY_FEATURES, name = "PeaceCandle",
			comment = "Enable peace candle generation. Chance: (Default = 3, lower is more common)") public static boolean PEACE_CANDLE =
					true;
	@ConfigOption(category = CATEGORY_FEATURES,
			name = "PeaceCandleChance") public static int PEACE_CANDLE_CHANCE = 3;

	@ConfigOption(category = CATEGORY_FEATURES, name = "AncientFurnace",
			comment = "Enable ancient furnace generation. Chance: (Default = 2000, lower is more common)") public static boolean ANCIENT_FURNACE =
					true;
	@ConfigOption(category = CATEGORY_FEATURES,
			name = "AncientFurnaceChance") public static int ANCIENT_FURNACE_CHANCE = 2000;

	// Loot - Items found in chests and structures
	@ConfigOption(category = CATEGORY_LOOT, name = "MagicHood",
			comment = "Enable magic hood loot generation. Chance: (Default = 5, lower is more common)")
	public static boolean MAGIC_HOOD = true;
	@ConfigOption(category = CATEGORY_LOOT,
			name = "MagicHoodChance") public static int MAGIC_HOOD_CHANCE = 5;

	@ConfigOption(category = CATEGORY_LOOT, name = "SummoningPendulum",
			comment = "Enable summoning pendulum loot generation. Chance: (Default = -1, lower is more common)")
	public static boolean SUMMONING_PENDULUM = true;
	@ConfigOption(category = CATEGORY_LOOT,
			name = "SummoningPendulumChance") public static int SUMMONING_PENDULUM_CHANCE = 10;

	@ConfigOption(category = CATEGORY_LOOT, name = "BiomeCrystal",
			comment = "Enable biome crystal loot generation. Chance: (Default = 20, lower is more common)")
	public static boolean BIOME_CRYSTAL = true;
	@ConfigOption(category = CATEGORY_LOOT,
			name = "BiomeCrystalChance") public static int BIOME_CRYSTAL_CHANCE = 20;

	@ConfigOption(category = CATEGORY_LOOT, name = "LavaCharm",
			comment = "Enable lava charm loot generation. Chance: (Default = 5, lower is more common)")
	public static boolean LAVA_CHARM = true;
	@ConfigOption(category = CATEGORY_LOOT,
			name = "LavaCharmChance") public static int LAVA_CHARM_CHANCE = 5;

	@ConfigOption(category = CATEGORY_LOOT, name = "SlimeCube",
			comment = "Enable slime cube loot generation. Chance: (Default = 10, lower is more common)")
	public static boolean SLIME_CUBE = true;
	@ConfigOption(category = CATEGORY_LOOT,
			name = "SlimeCubeChance") public static int SLIME_CUBE_CHANCE = 10;

	@ConfigOption(category = CATEGORY_LOOT, name = "NumberedSpectreCoils",
			comment = "Enable numbered spectre coils loot generation. Chance: (Default = -1, lower is more common)")
	public static boolean NUMBERED_COILS = true;
	@ConfigOption(category = CATEGORY_LOOT,
			name = "NumberedSpectreCoilsChance") public static int NUMBERED_COILS_CHANCE = 10;
}
