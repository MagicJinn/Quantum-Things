package lumien.randomthings.item.block;

import lumien.randomthings.block.BlockPlatform;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockPlatform extends ItemBlock
{
	public ItemBlockPlatform(Block block)
	{
		super(block);

		this.setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int damage)
	{
		return damage;
	}

	@Override
	public String getTranslationKey(ItemStack stack)
	{
		return super.getTranslationKey() + "."
				+ BlockPlatform.EnumType.byMetadata(stack.getItemDamage()).getTranslationKey();
	}
}
