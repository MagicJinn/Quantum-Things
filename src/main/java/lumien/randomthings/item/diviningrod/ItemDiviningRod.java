package lumien.randomthings.item.diviningrod;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Level;
import lumien.randomthings.RandomThings;
import lumien.randomthings.config.DiviningRods;
import lumien.randomthings.handler.DiviningRodHandler;
import lumien.randomthings.handler.compability.jei.DescriptionHandler;
import lumien.randomthings.item.ItemBase;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.lib.IRTItemColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class ItemDiviningRod extends ItemBase implements IRTItemColor
{
	public static List<RodType> types;
	public static Map<RodType, Boolean> availableTypes;
	public static CombinedRodType universalRod;

	static
	{
		types = new ArrayList<RodType>();
		availableTypes = new LinkedHashMap<RodType, Boolean>();
	}

	public ItemDiviningRod() {
		super("diviningRod");

		this.setHasSubtypes(true);
		this.setMaxStackSize(1);
	}

	public static void preInit() {
		// Load all divining rods from config (needed before model registration)
		loadConfigRods();

		// Create universal rod with all non-universal rods (needed before model registration)
		List<RodType> rods = new ArrayList<RodType>();
		for (RodType type : types) {
			if (!(type instanceof CombinedRodType) && !type.getName().equals("universal")) {
				rods.add(type);
			}
		}
		universalRod = new CombinedRodType("universal", rods.toArray(new RodType[0]));
		types.add(universalRod);
	}

	public static void postInit() {
		// Check availability for all types
		types.stream().forEach((t) -> availableTypes.put(t, t.shouldBeAvailable()));

		// Register recipes for all divining rods
		registerRecipes();
	}

	private static void registerRecipes() {
		for (int i = 0; i < types.size(); i++) {
			RodType type = types.get(i);

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
				registerRodRecipe(oreType, i);
			}
		}
	}

	private static void registerRodRecipe(OreRodType rodType, int metadata) {
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

		net.minecraft.item.ItemStack stick =
				new net.minecraft.item.ItemStack(net.minecraft.init.Items.STICK);
		net.minecraft.item.ItemStack spiderEye =
				new net.minecraft.item.ItemStack(net.minecraft.init.Items.SPIDER_EYE);
		net.minecraft.item.ItemStack result =
				new net.minecraft.item.ItemStack(ModItems.diviningRod, 1, metadata);

		net.minecraft.util.ResourceLocation recipeName = new net.minecraft.util.ResourceLocation(
				"randomthings", "diviningrod_" + rodType.getName());
		net.minecraftforge.oredict.ShapedOreRecipe recipe =
				new net.minecraftforge.oredict.ShapedOreRecipe(recipeName, result, "RSR", "SES",
						"S S", 'R', ingredient, 'S', stick, 'E', spiderEye);
		recipe.setRegistryName(recipeName);
		net.minecraftforge.fml.common.registry.ForgeRegistries.RECIPES.register(recipe);
	}

	private static void registerUniversalRecipe() {
		// Universal rod recipe uses the first 8 valid rods in a 3x3 pattern
		List<net.minecraft.item.ItemStack> rodStacks =
				new ArrayList<net.minecraft.item.ItemStack>();
		for (int i = 0; i < types.size(); i++) {
			RodType type = types.get(i);
			if (!(type instanceof CombinedRodType) && !type.getName().equals("universal")
					&& availableTypes.get(type)) {
				rodStacks.add(new net.minecraft.item.ItemStack(ModItems.diviningRod, 1, i));
				if (rodStacks.size() >= 8) {
					break; // Only need first 8
				}
			}
		}

		// Find universal rod index
		int universalIndex = -1;
		for (int i = 0; i < types.size(); i++) {
			if (types.get(i).getName().equals("universal")) {
				universalIndex = i;
				break;
			}
		}

		if (universalIndex == -1) {
			return;
		}

		net.minecraft.item.ItemStack result =
				new net.minecraft.item.ItemStack(ModItems.diviningRod, 1, universalIndex);
		net.minecraft.item.ItemStack stick =
				new net.minecraft.item.ItemStack(net.minecraft.init.Items.STICK);
		net.minecraft.item.ItemStack slimeBall =
				new net.minecraft.item.ItemStack(net.minecraft.init.Items.SLIME_BALL);

		// Pattern: CSD, IBE, GLR (8 rod positions: C, I, G, L, R, E, D, S)
		// S can be a rod (8th) or stick if less than 8 rods available
		// B=slimeBall (center)
		net.minecraft.util.ResourceLocation recipeName =
				new net.minecraft.util.ResourceLocation("randomthings", "diviningrod_universal");

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
		net.minecraftforge.oredict.ShapedOreRecipe recipe =
				new net.minecraftforge.oredict.ShapedOreRecipe(recipeName, result, "CSD", "IBE",
						"GLR", 'C', c, 'S', s, 'D', d, 'I', i, 'B', slimeBall, 'E', e, 'G', g, 'L',
						l, 'R', r);
		recipe.setRegistryName(recipeName);
		net.minecraftforge.fml.common.registry.ForgeRegistries.RECIPES.register(recipe);
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
				RandomThings.logger.log(Level.INFO,
						"Added divining rod: " + name + " for " + oreName);
			} catch (NumberFormatException e) {
				RandomThings.logger.log(Level.WARN,
						"Invalid number format in divining rod entry: " + rodEntry);
			}
		}
	}

	private static String generateNameFromRecipeItem(String recipeItem) {
		// List of common suffixes/prefixes to remove
		String[] list = {"ingot", "gem", "crystal", "ore", "dust", "nugget", "block"};
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
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
	{
		if (this.isInCreativeTab(tab))
		{
			if (types == null || availableTypes == null)
			{
				return;
			}
			for (int i = 0; i < types.size(); i++) {
				RodType type = types.get(i);
				if (type == null) {
					continue;
				}
				Boolean available = availableTypes.get(type);
				if (available != null && available)
				{
					items.add(new ItemStack(this, 1, i));
				}
			}
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		RodType type = types.get(stack.getItemDamage());
		if (type.getName().equals("universal")) {
			return "item.diviningRodUniversal";
		}
		return "item.diviningRod";
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		RodType type = types.get(stack.getItemDamage());

		if (type.getName().equals("universal")) {
			String universalName = I18n.translateToLocal("item.diviningRodUniversal.name");
			String rodName = I18n.translateToLocal("item.diviningRod.name");
			return universalName + " " + rodName;
		}

		if (type instanceof OreRodType) {
			OreRodType oreType = (OreRodType) type;
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
				// Only remove dimension if it has a space after it (avoid Netherite, Ender Essence)
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
	public boolean hasEffect(ItemStack stack)
	{
		EntityPlayer player = Minecraft.getMinecraft().player;

		if (player != null)
		{
			if (player.getHeldItemMainhand() == stack || player.getHeldItemOffhand() == stack)
			{
				RodType type = getRodType(stack);

				return DiviningRodHandler.get().shouldGlow(type);
			}
		}

		return super.hasEffect(stack);
	}

	public static RodType getRodType(ItemStack stack)
	{
		return types.get(stack.getItemDamage());
	}

	@Override
	public int getColorFromItemstack(ItemStack stack, int tintIndex)
	{
		int meta = stack.getItemDamage();
		
		if (tintIndex == 1 && meta < types.size())
		{
			return types.get(meta).getItemColor().getRGB();
		}
		else
		{
			return Color.WHITE.getRGB();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip,
			ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);

		int meta = stack.getItemDamage();
		if (meta >= types.size()) {
			return;
		}

		RodType rodType = types.get(meta);
		String description = DescriptionHandler.getDiviningRodDescription(rodType);
		tooltip.add(description);
	}
}
