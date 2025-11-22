package lumien.randomthings.config;

import lumien.randomthings.lib.ConfigOption;

public class SpectreCoils {
	public static final String CATEGORY = "Spectre Coils and Chargers";

	@ConfigOption(category = CATEGORY, name = "NumberedSpectreCoilEnergy", comment = "How much Energy a Numbered Spectre Coil produces per Tick")
	public static int NUMBERED_SPECTRECOIL_ENERGY = 128;

	@ConfigOption(category = CATEGORY, name = "EnergyTransferMultiplier", comment = "Multiplier for max energy transfer rate of Spectre Coils and Chargers (1.0 = default rates: Normal=1024, Redstone=4096, Ender=20480)")
	public static double ENERGY_TRANSFER_MULTIPLIER = 1.0;
	
	@ConfigOption(category = CATEGORY, name = "GenesisSpectreGeneratesEnergy",
			comment = "Whether the Genesis Spectre Coil generates energy, or just transfers energy from the player's Injector with no transfer limit")
    public static boolean GENESIS_SPECTRE_GENERATES_ENERGY = true;

    @ConfigOption(category = CATEGORY, name = "SpectreEnergyInjectorMaxEnergy", comment = "The maximum energy the Spectre Energy Injector can hold")
    public static int SPECTRE_ENERGY_INJECTOR_MAX_ENERGY = 1000000;
}

