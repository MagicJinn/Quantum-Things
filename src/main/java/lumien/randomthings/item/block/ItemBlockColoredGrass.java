package lumien.randomthings.item.block;

import lumien.randomthings.RandomThings;
import lumien.randomthings.lib.IRTItemColor;
import net.minecraft.block.Block;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockColoredGrass extends ItemBlock implements IRTItemColor
{

	public ItemBlockColoredGrass(Block block)
	{
		super(block);

		this.setHasSubtypes(true);
		RandomThings.proxy.scheduleColor(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemstack(ItemStack stack, int tintIndex)
	{
		return ItemDye.DYE_COLORS[EnumDyeColor.byMetadata(stack.getItemDamage()).getDyeDamage()];
	}

	@Override
	public int getMetadata(int damage)
	{
		return damage;
	}

	@Override
	public String getTranslationKey(ItemStack stack)
	{
		return super.getTranslationKey() + "." + EnumDyeColor.byMetadata(stack.getMetadata()).getTranslationKey();
	}
}
