package lumien.randomthings.config;

import lumien.randomthings.lib.ConfigOption;

public class Visual {
        public static final String CATEGORY = "Visual";
        @ConfigOption(category = CATEGORY, name = "FlatRunes",
            comment = "Replaces the noisy default rune texture with a flat version") public static boolean FLAT_RUNES =
                    false;

        @ConfigOption(category = CATEGORY, name = "HideCoordinates",
            comment = "When set to true the coordinates a position filter / portkey point to won't be displayed in its tooltip.") public static boolean HIDE_CORDS =
                    false;

        @ConfigOption(category = CATEGORY, name = "RemoveUnderwaterTexture",
                        comment = "TRIES to remove the weird water texture showing around ALL non full blocks. This might look weird when you, for example, are on a ladder underwater.") public static boolean removeAirBubble =
                                        false;

        @ConfigOption(category = CATEGORY, name = "FancySpectreArmorTransparency", comment = "When set to true, will scale the player models transparency based on the amount of Spectre Armor pieces they are wearing. Else, it will be a flat 50% trnaparency when wearing the full set.")
        public static boolean FANCY_SPECTRE_ARMOR_TRANSPARENCY = true;
}

