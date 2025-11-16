package lumien.randomthings.config;

import lumien.randomthings.lib.ConfigOption;

public class Features
{
	@ConfigOption(category = "Features", name = "RemoveUnderwaterTexture",
			comment = "TRIES to remove the weird water texture showing around ALL non full blocks. This might look weird when you, for example, are on a ladder underwater.")
	public static boolean removeAirBubble = false;

	@ConfigOption(category = "Features",
			name = "ArtificialEndPortal")
	public static boolean ARTIFICIAL_END_PORTAL = true;

	@ConfigOption(category = "Features", name = "EnderAnchorChunkloading",
			comment = "Should Ender Anchors keep the Chunk they are in loaded")
	public static boolean ENDER_ANCHOR_CHUNKLOADING = true;

	@ConfigOption(category = "Features", name = "GoldenEgg",
			comment = "Should there be an Golden Egg in every Bean Pod?")
	public static boolean GOLDEN_EGG = true;
	
	@ConfigOption(category = "Features", name = "MagneticEnchantment",
			comment = "Whether the magnetic enchantment should be available.")
	public static boolean MAGNETIC_ENCHANTMENT = true;
}
