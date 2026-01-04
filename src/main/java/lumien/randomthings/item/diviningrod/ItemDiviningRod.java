package lumien.randomthings.item.diviningrod;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.logging.log4j.Level;
import lumien.randomthings.RandomThings;
import lumien.randomthings.config.DiviningRods;
import lumien.randomthings.handler.DiviningRodHandler;
import lumien.randomthings.handler.compability.jei.DescriptionHandler;
import lumien.randomthings.item.ItemBase;
import lumien.randomthings.lib.IRTItemColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ItemDiviningRod extends ItemBase implements IRTItemColor {
	public static List<RodType> types;
	public static Map<RodType, Boolean> availableTypes;
	public static CombinedRodType universalRod;
	public static Map<RodType, ItemDiviningRod> rodItems;
	public static Map<Item, RodType> itemToRodType;
	public static int baseDurability = 1024;

	private RodType rodType;

	static {
		types = new ArrayList<RodType>();
		availableTypes = new LinkedHashMap<RodType, Boolean>();
		rodItems = new LinkedHashMap<RodType, ItemDiviningRod>();
		itemToRodType = new HashMap<Item, RodType>();
	}

	public ItemDiviningRod(String name, RodType rodType) {
		super(name);
		this.rodType = rodType;
		this.setMaxStackSize(1);
		if (rodType != null && rodType.getName().equals("universal")) {
			this.setMaxDamage(baseDurability * 8);
		} else {
			this.setMaxDamage(baseDurability);
		}
	}

	public static void preInit() {
		// Load all divining rods from config (needed before model registration)
		loadConfigRods();

		// Create universal rod with all non-universal rods
		List<RodType> rods = new ArrayList<RodType>();
		for (RodType type : types) {
			if (!(type instanceof CombinedRodType) && !type.getName().equals("universal")) {
				rods.add(type);
			}
		}
		universalRod = new CombinedRodType("universal", rods.toArray(new RodType[0]));
		types.add(universalRod);

		// Create individual item instances for each rod type
		for (RodType type : types) {
			String itemName = type.getName() + "diviningrod";
			ItemDiviningRod item = new ItemDiviningRod(itemName, type);
			rodItems.put(type, item);
			itemToRodType.put(item, type);
		}
	}

	public static void postInit() {
		// Check availability for all types
		types.stream().forEach((t) -> availableTypes.put(t, t.shouldBeAvailable()));

		// Register recipes for all divining rods
		registerRecipes();
	}

	private static void registerRecipes() {
		for (RodType type : types) {
			// Skip universal rod - it has a special recipe
			if (type.getName().equals("universal")) {
				registerUniversalRecipe();
				continue;
			}

			// Skip if not available
			if (!availableTypes.get(type)) {
				continue;
			}

			if (type instanceof OreRodType) {
				OreRodType oreType = (OreRodType) type;
				registerRodRecipe(oreType, type);
			}
		}
	}

	private static void registerRodRecipe(OreRodType rodType, RodType type) {
		String recipeItem = rodType.getRecipeItem();
		Object ingredient;

		// Check if it's an item string (contains colon) or an ore dict entry
		if (recipeItem.contains(":")) {
			// Parse item string (format: modid:itemname or modid:itemname:metadata)
			String[] itemParts = recipeItem.split(":");
			if (itemParts.length < 2) {
				RandomThings.logger.log(Level.WARN, "Invalid recipe item format for divining rod "
						+ rodType.getName() + ": " + recipeItem);
				return;
			}

			net.minecraft.item.Item item = net.minecraft.item.Item.getByNameOrId(recipeItem);
			if (item == null) {
				RandomThings.logger.log(Level.WARN, "Could not find item for divining rod "
						+ rodType.getName() + ": " + recipeItem);
				return;
			}

			int itemMeta = 0;
			if (itemParts.length > 2) {
				try {
					itemMeta = Integer.parseInt(itemParts[2]);
				} catch (NumberFormatException e) {
					RandomThings.logger.log(Level.WARN,
							"Invalid metadata in recipe item for divining rod " + rodType.getName()
									+ ": " + recipeItem);
				}
			}

			ingredient = new net.minecraft.item.ItemStack(item, 1, itemMeta);
		} else {
			// Use ore dictionary
			ingredient = recipeItem;
		}

		ItemStack stick = new ItemStack(net.minecraft.init.Items.STICK);
		ItemStack spiderEye = new ItemStack(net.minecraft.init.Items.SPIDER_EYE);
		ItemDiviningRod rodItem = rodItems.get(type);
		if (rodItem == null) {
			RandomThings.logger.log(Level.WARN, "Could not find item for divining rod type: " + rodType.getName());
			return;
		}
		ItemStack result = new ItemStack(rodItem, 1);

		ResourceLocation recipeName = new ResourceLocation(
				"randomthings", "diviningrod_" + rodType.getName());
		ShapedOreRecipe recipe = new ShapedOreRecipe(recipeName,
				result, "RSR", "SES",
				"S S", 'R', ingredient, 'S', stick, 'E', spiderEye);
		recipe.setRegistryName(recipeName);
		net.minecraftforge.fml.common.registry.ForgeRegistries.RECIPES.register(recipe);
	}

	private static void registerUniversalRecipe() {
		// Universal rod recipe uses the first 8 valid rods in a 3x3 pattern
		List<ItemStack> rodStacks = new ArrayList<ItemStack>();
		for (RodType type : types) {
			if (!(type instanceof CombinedRodType) && !type.getName().equals("universal")
					&& availableTypes.get(type)) {
				ItemDiviningRod rodItem = rodItems.get(type);
				if (rodItem != null) {
					rodStacks.add(new ItemStack(rodItem, 1));
					if (rodStacks.size() >= 8) {
						break; // Only need first 8
					}
				}
			}
		}

		// Find universal rod
		RodType universalType = null;
		for (RodType type : types) {
			if (type.getName().equals("universal")) {
				universalType = type;
				break;
			}
		}

		if (universalType == null) {
			return;
		}

		ItemDiviningRod universalItem = rodItems.get(universalType);
		if (universalItem == null) {
			return;
		}

		net.minecraft.item.ItemStack result = new net.minecraft.item.ItemStack(universalItem, 1);
		ItemStack stick = new ItemStack(Items.STICK);
		ItemStack slimeBall = new ItemStack(Items.SLIME_BALL);

		// Pattern: CSD, IBE, GLR (8 rod positions: C, I, G, L, R, E, D, S)
		// S can be a rod (8th) or stick if less than 8 rods available
		// B=slimeBall (center)
		ResourceLocation recipeName = new ResourceLocation("randomthings", "diviningrod_universal");

		// Get ingredients - use rods if available, otherwise use sticks
		Object c = rodStacks.size() > 0 ? rodStacks.get(0) : stick;
		Object s = rodStacks.size() > 1 ? rodStacks.get(1) : stick; // S position (top center)
		Object d = rodStacks.size() > 2 ? rodStacks.get(2) : stick;
		Object i = rodStacks.size() > 3 ? rodStacks.get(3) : stick;
		Object e = rodStacks.size() > 4 ? rodStacks.get(4) : stick;
		Object g = rodStacks.size() > 5 ? rodStacks.get(5) : stick;
		Object l = rodStacks.size() > 6 ? rodStacks.get(6) : stick;
		Object r = rodStacks.size() > 7 ? rodStacks.get(7) : stick;

		// Create recipe with pattern: CSD, IBE, GLR
		// C, S, D, I, E, G, L, R are rod positions (or sticks if not enough rods)
		// B is slimeBall (center)
		ShapedOreRecipe recipe = new ShapedOreRecipe(recipeName, result, "CSD", "IBE", "GLR", 'C', c, 'S', s, 'D', d,
				'I', i, 'B', slimeBall, 'E', e, 'G', g, 'L', l, 'R', r);
		recipe.setRegistryName(recipeName);
		ForgeRegistries.RECIPES.register(recipe);
	}

	private static void loadConfigRods() {
		Configuration config = RandomThings.instance.configuration.getConfiguration();
		if (config == null) {
			return;
		}

		String[] configRods = config.getStringList("Divining Rods", "Divining Rods",
				DiviningRods.DEFAULT_RODS, DiviningRods.PROPERTY_COMMENT);

		for (String rodEntry : configRods) {
			if (rodEntry == null || rodEntry.trim().isEmpty()) {
				continue;
			}

			String[] parts = rodEntry.split(",");
			if (parts.length != 5) {
				RandomThings.logger.log(Level.WARN, "Invalid divining rod entry: " + rodEntry
						+ ". Expected format: oreDictionaryName,recipeItem,red,green,blue");
				continue;
			}

			try {
				String oreName = parts[0].trim();
				String recipeItem = parts[1].trim();
				int red = Integer.parseInt(parts[2].trim());
				int green = Integer.parseInt(parts[3].trim());
				int blue = Integer.parseInt(parts[4].trim());

				// Generate name from recipe item
				String name = generateNameFromRecipeItem(recipeItem);

				// Clamp color values to the valid 0-254 range instead of skipping
				if (red < 0 || red > 254 || green < 0 || green > 254 || blue < 0 || blue > 254) {
					RandomThings.logger.log(Level.WARN, "Clamping color values for divining rod "
							+ name + ". Values must be between 0 and 254.");
					red = Math.max(0, Math.min(254, red));
					green = Math.max(0, Math.min(254, green));
					blue = Math.max(0, Math.min(254, blue));
				}

				// Check if rod with this name already exists
				boolean exists = false;
				for (RodType type : types) {
					if (type.getName().equals(name)) {
						exists = true;
						break;
					}
				}

				if (exists) {
					// No log, since duplicate rods are allowed by design
					continue;
				}

				Color color = new Color(red, green, blue, 50);
				types.add(new OreRodType(name, oreName, recipeItem, color));
			} catch (NumberFormatException e) {
				RandomThings.logger.log(Level.WARN,
						"Invalid number format in divining rod entry: " + rodEntry);
			}
		}
		String addedRodPrint = "Added divining rods: ";
		for (RodType type : types) {
			addedRodPrint += type.getName() + ", ";
		}
		RandomThings.logger.log(Level.INFO, addedRodPrint.substring(0, addedRodPrint.length() - 2));
	}

	private static String generateNameFromRecipeItem(String recipeItem) {
		// List of common suffixes/prefixes to remove
		String[] list = { "ingot", "gem", "crystal", "ore", "dust", "nugget", "block" };
		String name;
		if (recipeItem.contains(":")) {
			String[] parts = recipeItem.split(":");
			if (parts.length >= 2) {
				name = parts[1];
			} else {
				name = recipeItem;
			}
		} else {
			name = recipeItem;
		}
		// Always remove underscores
		name = name.replace("_", "");
		// Remove any prefix or suffix in the list
		// Remove prefix
		for (String s : list) {
			if (name.startsWith(s)) {
				name = name.substring(s.length());
				break;
			}
		}
		// Remove suffix
		for (String s : list) {
			if (name.endsWith(s)) {
				name = name.substring(0, name.length() - s.length());
				break;
			}
		}
		return name.toLowerCase();
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (this.isInCreativeTab(tab)) {
			if (rodType != null && availableTypes != null) {
				Boolean available = availableTypes.get(rodType);
				if (available != null && available) {
					items.add(new ItemStack(this, 1));
				}
			}
		}
	}

	@Override
	public String getTranslationKey(ItemStack stack) {
		if (rodType != null && rodType.getName().equals("universal")) {
			return "item.diviningRodUniversal";
		}
		return "item.diviningRod";
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if (rodType == null) {
			return super.getItemStackDisplayName(stack);
		}

		if (rodType.getName().equals("universal")) {
			String universalName = I18n.translateToLocal("item.diviningRodUniversal.name");
			String rodName = I18n.translateToLocal("item.diviningRod.name");
			return universalName + " " + rodName;
		}

		if (rodType instanceof OreRodType) {
			OreRodType oreType = (OreRodType) rodType;
			List<net.minecraft.item.ItemStack> ores = OreDictionary.getOres(oreType.oreName);

			if (!ores.isEmpty()) {
				// Get the first ore's display name
				net.minecraft.item.ItemStack firstOre = ores.get(0);
				String oreDisplayName = firstOre.getDisplayName();

				// Remove any existing " Ore" suffix if present
				if (oreDisplayName.endsWith(" Ore")) {
					oreDisplayName = oreDisplayName.substring(0, oreDisplayName.length() - 4);
				}

				// Remove dimension prefixes (Overworld, Nether, End) from display name
				// Only remove dimension if it has a space after it (avoid Netherite, Ender
				// Essence)
				oreDisplayName = oreDisplayName.replace("Overworld", "").replace("Nether ", "")
						.replace("End ", "").trim();

				String rodName = I18n.translateToLocal("item.diviningRod.name");
				return oreDisplayName + " " + rodName;
			}
		}

		// Fallback to default
		return super.getItemStackDisplayName(stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack) {
		EntityPlayer player = Minecraft.getMinecraft().player;

		if (player != null) {
			if (player.getHeldItemMainhand() == stack || player.getHeldItemOffhand() == stack) {
				RodType type = getRodType(stack);
				if (type != null) {
					return DiviningRodHandler.get().shouldGlow(type);
				}
			}
		}

		return super.hasEffect(stack);
	}

	public static RodType getRodType(ItemStack stack) {
		if (stack.isEmpty()) {
			return null;
		}
		Item item = stack.getItem();
		if (item instanceof ItemDiviningRod) {
			return ((ItemDiviningRod) item).rodType;
		}
		return itemToRodType.get(item);
	}

	@Override
	public int getColorFromItemstack(ItemStack stack, int tintIndex) {
		if (tintIndex == 1 && rodType != null) {
			return rodType.getItemColor().getRGB();
		} else {
			return Color.WHITE.getRGB();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip,
			@Nonnull ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);

		if (rodType != null) {
			String description = DescriptionHandler.getDiviningRodDescription(rodType);
			tooltip.add(description);
		}
	}

	@Override
	public void onUpdate(@Nonnull ItemStack stack, @Nonnull World worldIn, @Nonnull Entity entityIn, int itemSlot,
			boolean isSelected) {
		if (worldIn.isRemote)
			return;

		// Only apply durability when the rod is being held (main hand or offhand)
		if (!(entityIn instanceof EntityPlayer))
			return;

		EntityPlayer player = (EntityPlayer) entityIn;
		ItemStack mainHand = player.getHeldItemMainhand();
		ItemStack offHand = player.getHeldItemOffhand();

		// Check if this rod is being held (main hand selected or offhand)
		boolean isHeld = (isSelected && !mainHand.isEmpty() && mainHand.getItem() == this)
				|| (!offHand.isEmpty() && offHand.getItem() == this);
		if (!isHeld)
			return;

		// Skip if durability usage is disabled
		if (DiviningRods.DURABILITY_USAGE_SECONDS <= 0.0 || !stack.isItemStackDamageable()
				|| stack.getItemDamage() > stack.getMaxDamage())
			return;

		// Calculate chance per tick based on DURABILITY_USAGE_SECONDS
		// If DURABILITY_USAGE_SECONDS = 1.0, that's 1 durability per second = 1/20 =
		// 0.05 chance per tick
		// If DURABILITY_USAGE_SECONDS = 2.0, that's 0.5 durability per second = 0.5/20
		// = 0.025 chance per tick
		double chancePerTick = (1.0 / DiviningRods.DURABILITY_USAGE_SECONDS) / 20.0;

		// Damage item if the random chance is met
		if (worldIn.rand.nextDouble() < chancePerTick) {
			stack.damageItem(1, player);
		}
	}

	@Override
	public boolean getIsRepairable(@Nonnull ItemStack toRepair, @Nonnull ItemStack repair) {
		// Universal rod cannot be repaired
		if (rodType != null && rodType.getName().equals("universal"))
			return false;

		// Check if the repair item matches the recipe item
		if (rodType instanceof OreRodType) {
			OreRodType oreType = (OreRodType) rodType;
			String recipeItem = oreType.getRecipeItem();

			// Check if it's an item string (contains colon) or an ore dict entry
			if (recipeItem.contains(":")) {
				// Parse item string (format: modid:itemname or modid:itemname:metadata)
				String[] itemParts = recipeItem.split(":");
				if (itemParts.length >= 2) {
					net.minecraft.item.Item item = net.minecraft.item.Item.getByNameOrId(recipeItem);
					if (item != null) {
						int itemMeta = 0;
						if (itemParts.length > 2) {
							try {
								itemMeta = Integer.parseInt(itemParts[2]);
							} catch (NumberFormatException e) {
								// Use default meta
							}
						}
						// Check if repair item matches
						return repair.getItem() == item && repair.getItemDamage() == itemMeta;
					}
				}
			} else {
				// Use ore dictionary
				int[] repairOreIDs = net.minecraftforge.oredict.OreDictionary.getOreIDs(repair);
				String oreDictName = recipeItem;
				for (int oreID : repairOreIDs) {
					if (net.minecraftforge.oredict.OreDictionary.getOreName(oreID).equals(oreDictName)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	public boolean shouldCauseReequipAnimation(@Nonnull ItemStack oldStack, @Nonnull ItemStack newStack,
			boolean slotChanged) {
		// Prevent the stupid reequip animation every time you lose durability
		return oldStack.getItem() != newStack.getItem();
	}

	@Override
	public boolean isEnchantable(@Nonnull ItemStack stack) {
		// Not enchantable if durability is disabled
		return DiviningRods.DURABILITY_USAGE_SECONDS > 0.0;
	}

	@Override
	public int getItemEnchantability() {
		// Make unenchantable if durability is disabled
		if (DiviningRods.DURABILITY_USAGE_SECONDS <= 0.0)
			return 0;

		return 1;
	}

	@Override
	public boolean canApplyAtEnchantingTable(@Nonnull ItemStack stack, @Nonnull Enchantment enchantment) {
		// Not enchantable if durability is disabled
		if (DiviningRods.DURABILITY_USAGE_SECONDS <= 0.0)
			return false;

		// Allow Unbreaking and Mending enchantments
		return enchantment == Enchantments.UNBREAKING
				|| enchantment == Enchantments.MENDING;
	}

	@Override
	public boolean isBookEnchantable(@Nonnull ItemStack stack, @Nonnull ItemStack book) {
		// Not enchantable if durability is disabled
		if (DiviningRods.DURABILITY_USAGE_SECONDS <= 0.0)
			return false;

		// Only allow enchanted books with Unbreaking and/or Mendin
		if (book.isEmpty() || book.getItem() != Items.ENCHANTED_BOOK)
			return false;

		Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(book);

		// Book must contain at least one enchantment
		if (enchantments.isEmpty())
			return false;

		for (Enchantment ench : enchantments.keySet()) {
			if (ench != Enchantments.UNBREAKING && ench != Enchantments.MENDING) {
				return false;
			}
		}

		// If we got this far, the book has Unbreaking and/or Mending
		return true;
	}
}
