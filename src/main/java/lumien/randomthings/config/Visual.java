package lumien.randomthings.config;

import lumien.randomthings.lib.ConfigOption;

public class Visual {
    @ConfigOption(category = "Visual", name = "FlatRunes",
            comment = "Replaces the noisy default rune texture with a flat version") public static boolean FLAT_RUNES =
                    false;

    @ConfigOption(category = "Visual", name = "HideCoordinates",
            comment = "When set to true the coordinates a position filter / portkey point to won't be displayed in its tooltip.") public static boolean HIDE_CORDS =
                    false;
}

