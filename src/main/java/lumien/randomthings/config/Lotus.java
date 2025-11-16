package lumien.randomthings.config;

import lumien.randomthings.lib.ConfigOption;

public class Lotus {
    private static final String CATEGORY = "Lotus";

    @ConfigOption(category = CATEGORY, name = "CanBonemeal",
            comment = "Whether bonemeal can be used on lotus plants to grow them") public static boolean CAN_BONEMEAL =
                    false;

    @ConfigOption(category = CATEGORY, name = "XpAmount",
            comment = "Average amount of XP given when eating a lotus blossom (Default = 8") public static int XP_AMOUNT =
                    8;
}

