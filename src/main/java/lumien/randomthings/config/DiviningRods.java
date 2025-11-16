package lumien.randomthings.config;

public class DiviningRods {
        public static final String CONFIG_COMMENT =
                        "Divining Rods. Format: oreDictionaryName,recipeItem,red,green,blue. Values must be between 0 and 254. "
                                        + "Example: oreQuartz,minecraft:quartz,245,245,245. Recipe item can be an item (minecraft:quartz) or ore dict entry (ingotCopper). Name is auto-generated from recipe item. To disable a rod, simply remove its entry.";

        public static String[] DEFAULT_RODS = {
                        // Vanilla ores
                        "oreCoal,minecraft:coal,20,20,20", "oreIron,ingotIron,211,180,159",
                        "oreGold,ingotGold,246,233,80", "oreLapis,gemLapis,5,45,150",
                        "oreRedstone,dustRedstone,211,1,1", "oreEmerald,gemEmerald,0,220,0",
                        "oreDiamond,gemDiamond,87,221,229", "oreQuartz,gemQuartz,236,233,227",
                        // Thermal Foundation
                        "oreCopper,ingotCopper,252,113,21", "oreTin,ingotTin,150,184,217",
                        "oreSilver,ingotSilver,205,231,246", "oreLead,ingotLead,117,133,187",
                        "oreAluminum,ingotAluminum,197,197,202",
                        "oreNickel,ingotNickel,208,206,163", "orePlatinum,ingotPlatinum,42,183,252",
                        "oreIridium,ingotIridium,176,176,202", "oreMithril,ingotMithril,97,207,252",
                        // Draconic
                        "oreDraconium,ingotDraconium,75,38,107",
                        // Tinkers
                        "oreCobalt,ingotCobalt,5,18,64", "oreArdite,ingotArdite,138,104,38",
                        // Actually Additions
                        "oreQuartzBlack,gemQuartzBlack,10,10,10",
                        // Applied Energistics
                        "oreCertusQuartz,crystalCertusQuartz,136,166,193",
                        // Silent's Gems
                        "oreChaos,gemChaos,205,184,172", "oreAgate,gemAgate,202,28,202",
                        "oreAlexandrite,gemAlexandrite,107,107,107", "oreAmber,gemAmber,202,144,28",
                        "oreAmethyst,gemAmethyst,144,28,202", "oreAmetrine,gemAmetrine,128,0,65",
                        "oreAmmolite,gemAmmolite,174,65,195", "oreApatite,gemApatite,65,195,163",
                        "oreAquamarine,gemAquamarine,28,202,202",
                        "oreBlackDiamond,gemBlackDiamond,24,24,24",
                        "oreBlueTopaz,gemBlueTopaz,0,22,128", "oreCarnelian,gemCarnelian,128,11,0",
                        "oreCatsEye,gemCatsEye,195,152,65",
                        "oreChrysoprase,gemChrysoprase,87,195,65", "oreCitrine,gemCitrine,128,75,0",
                        "oreCoral,gemCoral,195,76,65", "oreFluorite,gemFluorite,65,163,195",
                        "oreGarnet,gemGarnet,202,72,28", "oreGoldenBeryl,gemGoldenBeryl,128,128,0",
                        "oreBeryl,gemBeryl,28,202,28", "oreHeliodor,gemHeliodor,202,173,28",
                        "oreIndicolite,gemIndicolite,28,202,115", "oreIolite,gemIolite,86,28,202",
                        "oreJade,gemJade,131,195,65", "oreJasper,gemJasper,128,107,0",
                        "oreKunzite,gemKunzite,195,65,174", "oreKyanite,gemKyanite,65,109,195",
                        "oreLepidolite,gemLepidolite,128,0,96",
                        "oreMalachite,gemMalachite,0,128,43", "oreMoldavite,gemMoldavite,107,128,0",
                        "oreMoonstone,gemMoonstone,0,86,128",
                        "oreMorganite,gemMorganite,239,100,210", "oreOnyx,gemOnyx,53,53,53",
                        "oreOpal,gemOpal,193,193,193", "orePearl,gemPearl,179,194,218",
                        "orePeridot,gemPeridot,144,202,28", "orePyrope,gemPyrope,195,65,98",
                        "oreRoseQuartz,gemRoseQuartz,228,110,154", "oreRuby,gemRuby,202,28,28",
                        "oreSapphire,gemSapphire,28,28,202", "oreSodalite,gemSodalite,76,65,195",
                        "oreSpinel,gemSpinel,128,54,0", "oreSunstone,gemSunstone,195,98,65",
                        "oreTanzanite,gemTanzanite,75,0,128", "oreTektite,gemTektite,123,104,87",
                        "oreTopaz,gemTopaz,202,101,28", "oreTurquoise,gemTurquoise,0,128,107",
                        "oreVioletSapphire,gemVioletSapphire,118,0,128",
                        "oreZircon,gemZircon,195,195,65"};

}
