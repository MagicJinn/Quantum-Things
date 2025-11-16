package lumien.randomthings.config;

public class DiviningRods {
    public static final String CONFIG_COMMENT =
            "Divining rods. Format: oreDictionaryName,recipeItem,red,green,blue. "
                    + "Example: oreQuartz,minecraft:quartz,245,245,245. Recipe item can be an item (minecraft:quartz) or ore dict entry (ingotCopper). Name is auto-generated from recipe item. To disable a rod, simply remove its entry.";

    public static String[] DEFAULT_RODS = {
            // Vanilla ores
            "oreCoal,minecraft:coal,20,20,20", "oreIron,ingotIron,211,180,159",
            "oreGold,ingotGold,246,233,80", "oreLapis,gemLapis,5,45,150",
            "oreRedstone,dustRedstone,211,1,1", "oreEmerald,gemEmerald,0,220,0",
            "oreDiamond,gemDiamond,87,221,229", "oreQuartz,gemQuartz,245,245,245",
            // Thermal Foundation
            "oreCopper,ingotCopper,252,113,21", "oreTin,ingotTin,150,184,217",
            "oreSilver,ingotSilver,205,231,246", "oreLead,ingotLead,117,133,187",
            "oreAluminum,ingotAluminum,197,197,202", "oreNickel,ingotNickel,208,206,163",
            "orePlatinum,ingotPlatinum,42,183,252", "oreIridium,ingotIridium,176,176,202",
            "oreMithril,ingotMithril,97,207,252",
            // Draconic
            "oreDraconium,ingotDraconium,75,38,107",
            // Tinkers
            "oreCobalt,ingotCobalt,5,18,64", "oreArdite,ingotArdite,138,104,38",
            // Actually Additions
            "oreQuartzBlack,gemQuartzBlack,10,10,10",
            // Applied Energistics
            "oreCertusQuartz,crystalCertusQuartz,136,166,193"};
}
