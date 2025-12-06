package lumien.randomthings.handler.compability.jei;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import lumien.randomthings.RandomThings;
import lumien.randomthings.block.BlockBase;
import lumien.randomthings.block.BlockSpectreCoil;
import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.config.SpectreCoils;
import lumien.randomthings.enchantment.ModEnchantments;
import lumien.randomthings.item.ItemBase;
import lumien.randomthings.item.ItemIngredient;
import lumien.randomthings.item.ItemSpectreCharger;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.config.Numbers;
import lumien.randomthings.item.diviningrod.ItemDiviningRod;
import lumien.randomthings.item.diviningrod.RodType;
import mezz.jei.api.IModRegistry;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.oredict.OreDictionary;

public class DescriptionHandler
{
	static IModRegistry registry;

	public static void addDescriptions(IModRegistry registry)
	{
		DescriptionHandler.registry = registry;

		Map<Object, String> overrideMap = new HashMap<>();
		overrideMap.put(ModBlocks.customWorkbench, "tile.customWorkbench.info");
		overrideMap.put(ModBlocks.specialChest, null);
		overrideMap.put(ModItems.rezStone, null);
		overrideMap.put(ModBlocks.platform, "tile.platform.info");
		overrideMap.put(ModBlocks.contactLever, "tile.contactButton.info");
		overrideMap.put(ModBlocks.luminousBlock, "tile.luminousBlock.info");
		overrideMap.put(ModBlocks.translucentLuminousBlock, "tile.translucentLuminousBlock.info");
		overrideMap.put(ModBlocks.biomeStone, "tile.biomeStone.info");
		overrideMap.put(ModBlocks.coloredGrass, "tile.coloredGrass.info");
		overrideMap.put(ModBlocks.stainedBrick, "tile.stainedBrick.info");
		overrideMap.put(ModBlocks.luminousStainedBrick, "tile.luminousStainedBrick.info");
		overrideMap.put(ModItems.grassSeeds, "item.grassSeeds.info");
		overrideMap.put(ModItems.runeDust, "item.runeDust.info");
		overrideMap.put(ModItems.spectreCharger, null);
		overrideMap.put(ModBlocks.ancientBrick, null);

		List<ItemStack> stackBlackList = new ArrayList<>();
		stackBlackList.add(new ItemStack(ModItems.ingredients, 1, ItemIngredient.INGREDIENT.BIOME_SENSOR.id));
		stackBlackList.add(new ItemStack(ModItems.ingredients, 1, ItemIngredient.INGREDIENT.EVIL_TEAR.id));
		stackBlackList.add(new ItemStack(ModItems.ingredients, 1, ItemIngredient.INGREDIENT.SPECTRE_INGOT.id));
		stackBlackList.add(new ItemStack(ModItems.ingredients, 1, ItemIngredient.INGREDIENT.SUPERLUBRICENT_TINCTURE.id));
		stackBlackList.add(new ItemStack(ModItems.ingredients, 1, ItemIngredient.INGREDIENT.FLOO_POWDER.id));
		stackBlackList.add(new ItemStack(ModItems.ingredients, 1, ItemIngredient.INGREDIENT.PLATE_BASE.id));
		stackBlackList.add(new ItemStack(ModItems.ingredients, 1, ItemIngredient.INGREDIENT.PRECIOUS_EMERALD.id));
		stackBlackList.add(new ItemStack(ModItems.ingredients, 1, ItemIngredient.INGREDIENT.SPECTRE_STRING.id));

		for (int i = 0; i < ItemDiviningRod.types.size(); i++)
		{
			ItemStack stack = new ItemStack(ModItems.diviningRod, 1, i);
			stackBlackList.add(stack);
			String description = getDiviningRodDescription(ItemDiviningRod.types.get(i));
			registry.addDescription(stack, description);
		}

		removeDes(overrideMap, ModBlocks.spectreLeaf, ModBlocks.natureCore, ModBlocks.spectreLog, ModBlocks.spectrePlank, ModBlocks.specialChest, ModBlocks.superLubricentPlatform, ModBlocks.filteredSuperLubricentPlatform);

		// Manually Add
		registry.addDescription(new ItemStack(ModBlocks.blockDiaphanous, 1, OreDictionary.WILDCARD_VALUE), "tile.diaphanousBlock.info");
		registry.addDescription(ItemEnchantedBook.getEnchantedItemStack(new EnchantmentData(ModEnchantments.magnetic, 1)), "enchantment.randomthings.magnetic.desc");

		// Add spectre coils to override map to handle them specially
		overrideMap.put(ModBlocks.spectreCoilNormal, null);
		overrideMap.put(ModBlocks.spectreCoilRedstone, null);
		overrideMap.put(ModBlocks.spectreCoilEnder, null);
		overrideMap.put(ModBlocks.spectreCoilNumber, null);
		overrideMap.put(ModBlocks.spectreCoilGenesis, null);

		// Add dynamic descriptions for spectre coils
		registry.addDescription(new ItemStack(ModBlocks.spectreCoilNormal, 1),
				getSpectreCoilDescription(BlockSpectreCoil.CoilType.NORMAL));
		registry.addDescription(new ItemStack(ModBlocks.spectreCoilRedstone, 1),
				getSpectreCoilDescription(BlockSpectreCoil.CoilType.REDSTONE));
		registry.addDescription(new ItemStack(ModBlocks.spectreCoilEnder, 1),
				getSpectreCoilDescription(BlockSpectreCoil.CoilType.ENDER));
		registry.addDescription(new ItemStack(ModBlocks.spectreCoilNumber, 1),
				getSpectreCoilDescription(BlockSpectreCoil.CoilType.NUMBER));
		registry.addDescription(new ItemStack(ModBlocks.spectreCoilGenesis, 1),
				getSpectreCoilDescription(BlockSpectreCoil.CoilType.GENESIS));

		// Add dynamic descriptions for spectre chargers
		registry.addDescription(
				new ItemStack(ModItems.spectreCharger, 1, ItemSpectreCharger.TIER.NORMAL.ordinal()),
				getSpectreChargerDescription(ItemSpectreCharger.TIER.NORMAL));
		registry.addDescription(
				new ItemStack(ModItems.spectreCharger, 1,
						ItemSpectreCharger.TIER.REDSTONE.ordinal()),
				getSpectreChargerDescription(ItemSpectreCharger.TIER.REDSTONE));
		registry.addDescription(
				new ItemStack(ModItems.spectreCharger, 1, ItemSpectreCharger.TIER.ENDER.ordinal()),
				getSpectreChargerDescription(ItemSpectreCharger.TIER.ENDER));
		registry.addDescription(
				new ItemStack(ModItems.spectreCharger, 1,
						ItemSpectreCharger.TIER.GENESIS.ordinal()),
				getSpectreChargerDescription(ItemSpectreCharger.TIER.GENESIS));

		Stream.concat(BlockBase.rtBlockList.stream(), ItemBase.rtItemList.stream()).forEach(new Consumer<Object>()
		{
			@Override
			public void accept(Object t)
			{
				NonNullList<ItemStack> subItems = NonNullList.create();
				if (t instanceof Item)
				{
					Item item = (Item) t;

					if (item.getCreativeTab() == RandomThings.instance.creativeTab)
					{
						item.getSubItems(RandomThings.instance.creativeTab, subItems);
					}
				}
				else if (t instanceof Block)
				{
					Block block = (Block) t;

					if (block.getCreativeTab() == RandomThings.instance.creativeTab)
					{
						block.getSubBlocks(RandomThings.instance.creativeTab, subItems);
					}
				}

				if (!subItems.isEmpty())
				{
					if (overrideMap.containsKey(t))
					{
						String override = overrideMap.get(t);

						if (override != null)
						{
							if (t instanceof Block)
							{
								registry.addDescription(new ItemStack((Block) t, 1, OreDictionary.WILDCARD_VALUE), override);
							}
							else if (t instanceof Item)
							{
								registry.addDescription(new ItemStack((Item) t, 1, OreDictionary.WILDCARD_VALUE), override);
							}
						}

						return;
					}
				}

				for (ItemStack is : subItems)
				{
					if (!is.isEmpty())
					{
						boolean blackListed = false;
						for (ItemStack b : stackBlackList)
						{
							if (ItemStack.areItemStacksEqual(b, is))
							{
								blackListed = true;
								break;
							}
						}

						if (!blackListed)
						{
							registry.addDescription(is, is.getTranslationKey() + ".info");
						}
					}
				}
			}
		});
	}

	private static void add(Item item, String key)
	{
		registry.addDescription(new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE), key);
	}

	private static void add(Block block, String key)
	{
		registry.addDescription(new ItemStack(block, 1, OreDictionary.WILDCARD_VALUE), key);
	}

	private static void add(ItemStack stack, String key)
	{
		registry.addDescription(stack, key);
	}

	private static void removeDes(Map<Object, String> overrideMap, Object... toRemove)
	{
		for (Object o : toRemove)
		{
			overrideMap.put(o, null);
		}
	}

	private static String formatOreName(String oreName) {
		String formatted = oreName.toLowerCase();

		// Remove "ore" prefix if present
		if (formatted.startsWith("ore")) {
			formatted = formatted.substring(3);
		}

		// Capitalize first letter and replace underscores with spaces
		if (!formatted.isEmpty()) {
			formatted = formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
			formatted = formatted.replace("_", " ");
		}

		return formatted;
	}

	public static String getDiviningRodDescription(RodType rodType) {
		boolean isUniversal = rodType.getName().equals("universal");
		String formattedOreName = formatOreName(rodType.getOreName());
		String oreKey = "item.diviningRod.ore";
		String oreTranslation = I18n.translateToLocal(oreKey);
		if (!isUniversal && !oreTranslation.isEmpty()) {
			oreTranslation =
					oreTranslation.substring(0, 1).toUpperCase() + oreTranslation.substring(1);
		}
		String oreName = formattedOreName + " " + oreTranslation;
		int range = Numbers.DIVINING_ROD_RANGE;
		String rangeString = range + " x " + range;
		String description = I18n.translateToLocalFormatted("item.diviningRod.info", oreName.trim(),
				rangeString);
		return description;
	}

	public static void refreshSpectreCoilDescriptions() {
		if (registry != null) {
			// Re-register all spectre coil descriptions with current config values
			// JEI will update the cached descriptions when re-registered
			registry.addDescription(new ItemStack(ModBlocks.spectreCoilNormal, 1),
					getSpectreCoilDescription(BlockSpectreCoil.CoilType.NORMAL));
			registry.addDescription(new ItemStack(ModBlocks.spectreCoilRedstone, 1),
					getSpectreCoilDescription(BlockSpectreCoil.CoilType.REDSTONE));
			registry.addDescription(new ItemStack(ModBlocks.spectreCoilEnder, 1),
					getSpectreCoilDescription(BlockSpectreCoil.CoilType.ENDER));
			registry.addDescription(new ItemStack(ModBlocks.spectreCoilNumber, 1),
					getSpectreCoilDescription(BlockSpectreCoil.CoilType.NUMBER));
			registry.addDescription(new ItemStack(ModBlocks.spectreCoilGenesis, 1),
					getSpectreCoilDescription(BlockSpectreCoil.CoilType.GENESIS));

			// Re-register all spectre charger descriptions with current config values
			registry.addDescription(
					new ItemStack(ModItems.spectreCharger, 1,
							ItemSpectreCharger.TIER.NORMAL.ordinal()),
					getSpectreChargerDescription(ItemSpectreCharger.TIER.NORMAL));
			registry.addDescription(
					new ItemStack(ModItems.spectreCharger, 1,
							ItemSpectreCharger.TIER.REDSTONE.ordinal()),
					getSpectreChargerDescription(ItemSpectreCharger.TIER.REDSTONE));
			registry.addDescription(
					new ItemStack(ModItems.spectreCharger, 1,
							ItemSpectreCharger.TIER.ENDER.ordinal()),
					getSpectreChargerDescription(ItemSpectreCharger.TIER.ENDER));
			registry.addDescription(
					new ItemStack(ModItems.spectreCharger, 1,
							ItemSpectreCharger.TIER.GENESIS.ordinal()),
					getSpectreChargerDescription(ItemSpectreCharger.TIER.GENESIS));
		}
	}

	public static String getSpectreCoilDescription(BlockSpectreCoil.CoilType coilType) {
		String actionKey;
		String amount;
		boolean isTransfer;

		switch (coilType) {
			case NORMAL:
				actionKey = "tile.spectrecoil.transfer";
				amount = String.valueOf((int) (1024 * SpectreCoils.ENERGY_TRANSFER_MULTIPLIER));
				isTransfer = true;
				break;
			case REDSTONE:
				actionKey = "tile.spectrecoil.transfer";
				amount = String.valueOf((int) (4096 * SpectreCoils.ENERGY_TRANSFER_MULTIPLIER));
				isTransfer = true;
				break;
			case ENDER:
				actionKey = "tile.spectrecoil.transfer";
				amount = String.valueOf((int) (20480 * SpectreCoils.ENERGY_TRANSFER_MULTIPLIER));
				isTransfer = true;
				break;
			case NUMBER:
				actionKey = "tile.spectrecoil.generate";
				amount = String.valueOf(SpectreCoils.NUMBERED_SPECTRECOIL_ENERGY);
				isTransfer = false;
				break;
			case GENESIS:
				if (SpectreCoils.GENESIS_SPECTRE_GENERATES_ENERGY) {
					actionKey = "tile.spectrecoil.generate";
					amount = "Infinite";
					isTransfer = false;
				} else {
					actionKey = "tile.spectrecoil.transfer";
					amount = "Infinite";
					isTransfer = true;
				}
				break;
			default:
				actionKey = "tile.spectrecoil.transfer";
				amount = "???";
				isTransfer = true;
				break;
		}

		String actionText = I18n.translateToLocalFormatted(actionKey, amount);

		if (coilType == BlockSpectreCoil.CoilType.NUMBER) {
			return actionText + I18n.translateToLocal("jei.spectreCoil.number.suffix");
		} else if (coilType == BlockSpectreCoil.CoilType.GENESIS) {
			if (isTransfer) {
				return actionText + I18n.translateToLocal("jei.spectreCoil.transfer.suffix");
			} else {
				return actionText + I18n.translateToLocal("jei.spectreCoil.generate.suffix");
			}
		} else {
			return actionText + I18n.translateToLocal("jei.spectreCoil.transfer.suffix");
		}
	}

	public static String getSpectreChargerDescription(ItemSpectreCharger.TIER tier) {
		String amount;

		switch (tier) {
			case NORMAL:
				amount = String.valueOf((int) (1024 * SpectreCoils.ENERGY_TRANSFER_MULTIPLIER));
				break;
			case REDSTONE:
				amount = String.valueOf((int) (4096 * SpectreCoils.ENERGY_TRANSFER_MULTIPLIER));
				break;
			case ENDER:
				amount = String.valueOf((int) (20480 * SpectreCoils.ENERGY_TRANSFER_MULTIPLIER));
				break;
			case GENESIS:
				amount = "Infinite";
				break;
			default:
				amount = "???";
				break;
		}

		return I18n.translateToLocalFormatted("item.spectreCharger.info", amount);
	}
}
