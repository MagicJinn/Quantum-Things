package lumien.randomthings.config;

import lumien.randomthings.lib.ConfigOption;

public class Features {
	public static final String CATEGORY = "Features";
	@ConfigOption(category = CATEGORY, name = "ArtificialEndPortal",
			comment = "Whether the player can create an artificial End Portal with an Evil Tear.") public static boolean ARTIFICIAL_END_PORTAL =
					true;

	@ConfigOption(category = CATEGORY, name = "EnderAnchorChunkloading",
			comment = "Should Ender Anchors keep the Chunk they are in loaded") public static boolean ENDER_ANCHOR_CHUNKLOADING =
					true;

	@ConfigOption(category = CATEGORY, name = "GoldenEgg",
			comment = "Should there be an Golden Egg in every Bean Pod?") public static boolean GOLDEN_EGG =
					true;

					@ConfigOption(category = CATEGORY, name = "GoldenChickenProduction",
			comment = "Should the Golden Chicken produce Golden Ingots automatically, or only when fed?") public static boolean GOLDEN_CHICKEN_PRODUCTION =
					false;

	@ConfigOption(category = CATEGORY, name = "MagneticEnchantment",
			comment = "Whether the magnetic enchantment should be available.") public static boolean MAGNETIC_ENCHANTMENT =
					true;

	@ConfigOption(category = CATEGORY, name = "SpectreDimension",
			comment = "Whether the Spectre Dimension should be enabled. If disabled, you will not be able to enter the dimension.") public static boolean SPECTRE_DIMENSION =
					true;

	@ConfigOption(category = CATEGORY, name = "EnableSpectreSapling",
			comment = "Whether the Spectre Sapling should be enabled. If disabled, the sapling will not grow and cannot be created from regular saplings.") public static boolean ENABLE_SPECTRE_SAPLING =
					true;

	@ConfigOption(category = CATEGORY, name = "AncientBrickDropItems", comment = "Whether Ancient Brick blocks can be broken and will drop their item forms.")
	public static boolean ANCIENT_BRICK_DROP_ITEMS = false;

	public static final String SUMMONING_PENDULUM_BLACKLIST_COMMENT = "List of entity resource locations that cannot be captured by the Summoning Pendulum. Format: modid:entityname. Example: minecraft:villager, randomthings:spirit.";

	public static String[] SUMMONING_PENDULUM_BLACKLIST = new String[0];

	@ConfigOption(category = CATEGORY, name = "DisableSpectreTools", comment = "Whether all Spectre Tools should be disabled. Removes the recipes and items from the game.")
	public static boolean DISABLE_SPECTRE_TOOLS = false;

	@ConfigOption(category = CATEGORY, name = "DisableSpectreArmor", comment = "Whether all Spectre Armor should be disabled. Removes the recipes and items from the game.")
	public static boolean DISABLE_SPECTRE_ARMOR = false;

	@ConfigOption(category = CATEGORY, name = "DisableSpectreIlluminator", comment = "Whether the Spectre Illuminator should be disabled. Removes its recipes/items/models and disables illumination logic.")
	public static boolean DISABLE_SPECTRE_ILLUMINATOR = false;

	@ConfigOption(category = CATEGORY, name = "LuminousBlocksEmitLight", comment = "Whether the Luminous and Luminous Translucent blocks should emit light.")
	public static boolean LUMINOUS_BLOCKS_EMIT_LIGHT = false;
}
