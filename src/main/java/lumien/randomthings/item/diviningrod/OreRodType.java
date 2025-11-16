package lumien.randomthings.item.diviningrod;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class OreRodType extends RodType
{
	private static final String ORE_PREFIX = "ore";
	private static final String[] DIMENSION_PREFIXES = {"Overworld", "Nether", "End"};

	String oreName;
	String recipeItem;

	int[] oreIDs; // Array of ore dictionary IDs to match (primary + dimension variants)
	Color color;

	ItemStack itemStack;

	public OreRodType(String name, String ore, String recipeItem, Color color)
	{
		super(name);

		this.oreName = ore;
		this.recipeItem = recipeItem;

		setupOreIDs(ore);

		this.color = color;
	}

	private void setupOreIDs(String ore) {
		// Build array of ore IDs to check (primary + variants if applicable)
		List<Integer> oreIDList = new ArrayList<Integer>();

		// Always add the primary ore ID
		int primaryOreID = OreDictionary.getOreID(ore);
		if (primaryOreID != -1) {
			oreIDList.add(primaryOreID);
		}

		// Only process ores that start with the ore prefix
		if (ore.startsWith(ORE_PREFIX)) {
			String baseSuffix = extractBaseSuffix(ore);
			String foundDimension = extractDimensionPrefix(ore);

			if (foundDimension != null) {
				// This is a dimension-specific ore (e.g., "oreNetherEmerald")
				// Also check the base variant
				String baseOreName = ORE_PREFIX + baseSuffix;
				int baseOreID = OreDictionary.getOreID(baseOreName);
				if (baseOreID != -1) {
					oreIDList.add(baseOreID);
				}
			} else {
				// This is a base ore (e.g., "oreEmerald")
				// Check all dimension variants
				for (String dimension : DIMENSION_PREFIXES) {
					String dimensionOreName = ORE_PREFIX + dimension + baseSuffix;
					int dimensionOreID = OreDictionary.getOreID(dimensionOreName);
					if (dimensionOreID != -1) {
						oreIDList.add(dimensionOreID);
					}
				}
			}
		}

		// Convert list to array
		this.oreIDs = new int[oreIDList.size()];
		for (int i = 0; i < oreIDList.size(); i++) {
			this.oreIDs[i] = oreIDList.get(i);
		}
	}

	/**
	 * Extracts the dimension prefix from an ore name (e.g., "Nether" from "oreNetherEmerald")
	 * 
	 * @param oreName The ore dictionary name
	 * @return The dimension prefix if found, null otherwise
	 */
	private String extractDimensionPrefix(String oreName) {
		for (String dimension : DIMENSION_PREFIXES) {
			String prefix = ORE_PREFIX + dimension;
			if (oreName.startsWith(prefix)) {
				return dimension;
			}
		}
		return null;
	}

	/**
	 * Extracts the base suffix from an ore name (e.g., "Emerald" from "oreEmerald" or
	 * "oreNetherEmerald")
	 * 
	 * @param oreName The ore dictionary name
	 * @return The suffix after "ore" or after "ore[Dimension]"
	 */
	private String extractBaseSuffix(String oreName) {
		// Try to find a dimension prefix first
		String dimension = extractDimensionPrefix(oreName);
		if (dimension != null) {
			// Remove "ore[Dimension]" to get the suffix
			return oreName.substring(ORE_PREFIX.length() + dimension.length());
		}
		// Otherwise, just remove "ore" prefix
		return oreName.substring(ORE_PREFIX.length());
	}

	public String getRecipeItem()
	{
		return recipeItem;
	}

	public String getOreName() {
		return oreName;
	}

	@Override
	public boolean matches(World worldObj, BlockPos pos, IBlockState state)
	{
		if (state.getBlock() != Blocks.AIR)
		{
			Item item = Item.getItemFromBlock(state.getBlock());

			int meta = state.getBlock().getMetaFromState(state);
			if (item != null)
			{
				ItemStack stack = new ItemStack(item, 1, meta);

				if (!stack.isEmpty())
				{
					int[] ids = OreDictionary.getOreIDs(stack);

					for (int blockOreID : ids)
					{
						for (int targetOreID : oreIDs) {
							if (blockOreID == targetOreID) {
								return true;
							}
						}
					}
				}
			}
		}

		return false;
	}

	@Override
	public boolean shouldBeAvailable()
	{
		return !OreDictionary.getOres(oreName).isEmpty();
	}

	@Override
	public Color getItemColor()
	{
		return color;
	}

}
