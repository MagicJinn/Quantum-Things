package lumien.randomthings.block;

import lumien.randomthings.config.Features;
import lumien.randomthings.item.block.ItemBlockClothLuminous;
import lumien.randomthings.lib.ILuminousBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BlockBlockLuminousBase extends BlockBase implements ILuminousBlock {
	public static final PropertyEnum<EnumDyeColor> COLOR = PropertyEnum.create("color", EnumDyeColor.class);

	protected BlockBlockLuminousBase(String name) {
		super(name, Material.GROUND, ItemBlockClothLuminous.class);

		this.setSoundType(SoundType.GLASS);
		this.setHardness(0.3F);
	}

	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(COLOR).getMetadata();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		EnumDyeColor[] aenumdyecolor = EnumDyeColor.values();
		int i = aenumdyecolor.length;

		for (int j = 0; j < i; ++j) {
			EnumDyeColor enumdyecolor = aenumdyecolor[j];
			list.add(new ItemStack(this, 1, enumdyecolor.getMetadata()));
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(COLOR, EnumDyeColor.byMetadata(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(COLOR).getMetadata();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { COLOR });
	}

	@Override
	public boolean shouldGlow(IBlockState state, int tintIndex) {
		return true;
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (Features.LUMINOUS_BLOCKS_EMIT_LIGHT) {
			return 15;
		}

		return super.getLightValue(state, world, pos);
	}
}
