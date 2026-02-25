package lumien.randomthings.item.block;

import lumien.randomthings.block.BlockAncientBrick;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockAncientBrick extends ItemBlock
{
	public ItemBlockAncientBrick(Block block)
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
				+ BlockAncientBrick.VARIANT.values()[stack.getMetadata()].getName();
	}
}
