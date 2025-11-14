package lumien.randomthings.config;

import lumien.randomthings.lib.ConfigOption;

public class Worldgen
{
	// Worldgen - Plants and natural generation
	@ConfigOption(category = "worldgen-plants", name = "Beans",
			comment = "Enable beans generation") public static boolean beans = true;

	@ConfigOption(category = "worldgen-plants", name = "BeansChance",
			comment = "Chance for beans generation (1 = 100%, 2 = 50%, etc.)") public static int BEANS_CHANCE =
					2;

	@ConfigOption(category = "worldgen-plants", name = "PitcherPlants",
			comment = "Enable pitcher plants generation") public static boolean pitcherPlants =
					true;

	@ConfigOption(category = "worldgen-plants", name = "PitcherPlantsChance",
			comment = "Chance for pitcher plants generation (10 = 1/10 chance)") public static int PITCHER_PLANTS_CHANCE =
					10;

	@ConfigOption(category = "worldgen-plants", name = "Lotus",
			comment = "Enable lotus generation") public static boolean LOTUS = true;

	@ConfigOption(category = "worldgen-plants", name = "LotusChance",
			comment = "Chance for lotus generation (10 = 1/10 chance)") public static int LOTUS_CHANCE =
					10;

	@ConfigOption(category = "worldgen-plants", name = "Sakanade",
			comment = "Enable sakanade generation") public static boolean sakanade = true;

	@ConfigOption(category = "worldgen-plants", name = "Nature-Core",
			comment = "Enable nature core generation") public static boolean natureCore = true;

	// Features - Structures and special blocks
	@ConfigOption(category = "worldgen-features", name = "WaterChest",
			comment = "Enable water chest generation") public static boolean WATER_CHEST = true;

	@ConfigOption(category = "worldgen-features", name = "PeaceCandle",
			comment = "Enable peace candle generation") public static boolean PEACE_CANDLE = true;

	@ConfigOption(category = "worldgen-features", name = "GlowingMushrooms",
			comment = "Enable glowing mushrooms generation") public static boolean GLOWING_MUSHROOM =
					true;

	@ConfigOption(category = "worldgen-features", name = "AncientFurnace",
			comment = "Enable ancient furnace generation") public static boolean ANCIENT_FURNACE =
					true;

	// Loot - Items found in chests and structures
	@ConfigOption(category = "worldgen-loot", name = "MagicHood",
			comment = "Enable magic hood loot generation")
	public static boolean MAGIC_HOOD = true;

	@ConfigOption(category = "worldgen-loot", name = "SummoningPendulum",
			comment = "Enable summoning pendulum loot generation")
	public static boolean SUMMONING_PENDULUM = true;

	@ConfigOption(category = "worldgen-loot", name = "BiomeCrystal",
			comment = "Enable biome crystal loot generation")
	public static boolean BIOME_CRYSTAL = true;

	@ConfigOption(category = "worldgen-loot", name = "LavaCharm",
			comment = "Enable lava charm loot generation")
	public static boolean LAVA_CHARM = true;

	@ConfigOption(category = "worldgen-loot", name = "SlimeCube",
			comment = "Enable slime cube loot generation")
	public static boolean SLIME_CUBE = true;
	
	@ConfigOption(category = "worldgen-loot", name = "NumberedSpectreCoils",
			comment = "Enable numbered spectre coils loot generation")
	public static boolean NUMBERED_COILS = true;
}
