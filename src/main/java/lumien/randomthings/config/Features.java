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
}
