package lumien.randomthings.config;

import lumien.randomthings.lib.ConfigOption;

public class NatureCore {
        private static final String CATEGORY = "Nature Core";

    // Sand Replacement
    @ConfigOption(category = CATEGORY, name = "SandReplacementChance",
            comment = "Chance for sand replacement (Default = 40, lower is more common)") public static int SAND_REPLACEMENT_CHANCE =
                    40;

    @ConfigOption(category = CATEGORY, name = "SandRange") public static int SAND_RANGE = 11;

    // Animal Spawning
    @ConfigOption(category = CATEGORY, name = "AnimalChance",
            comment = "Chance for animal spawning (Default = 400, lower is more common)") public static int ANIMAL_CHANCE =
                    400;

    @ConfigOption(category = CATEGORY, name = "AnimalRange") public static int ANIMAL_RANGE = 11;

    // Bonemealing
    @ConfigOption(category = CATEGORY, name = "BonemealChance",
            comment = "Chance for bonemealing (Default = 100, lower is more common)") public static int BONEMEAL_CHANCE =
                    100;

    @ConfigOption(category = CATEGORY, name = "BonemealRange") public static int BONEMEAL_RANGE =
            11;

    // Tree Spawning
    @ConfigOption(category = CATEGORY, name = "TreeChance",
                    comment = "Chance for tree spawning (Default = 600, lower is more common).") public static int TREE_CHANCE =
                    600;

    @ConfigOption(category = CATEGORY,
            name = "TreeRadiusRange") public static int TREE_RADIUS_RANGE = 20;

    // Shell Regeneration
    @ConfigOption(category = CATEGORY, name = "ShellRegenerationChance",
            comment = "Chance for the shell to regenerate (Default = 600, lower is more common)") public static int SHELL_REGENERATION_CHANCE =
                    600;
}

