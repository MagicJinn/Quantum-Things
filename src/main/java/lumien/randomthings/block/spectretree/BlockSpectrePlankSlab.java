package lumien.randomthings.block.spectretree;

import lumien.randomthings.RandomThings;
import lumien.randomthings.block.BlockBase;
import lumien.randomthings.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.Random;

public class BlockSpectrePlankSlab extends BlockSlab {
	public static final PropertyEnum<Variant> VARIANT = PropertyEnum.create("variant", Variant.class);

	public BlockSpectrePlankSlab(String registryName, BlockSpectrePlankSlab doubleSlabBlock) {
		super(Material.WOOD);

		this.setTranslationKey("spectreslab".equals(registryName) ? "spectreSlab" : registryName);
		this.setRegistryName(new ResourceLocation("randomthings", registryName));
		this.setCreativeTab(RandomThings.creativeTab);
		this.setHardness(2.0F);
		this.setResistance(5.0F);
		this.setSoundType(SoundType.WOOD);
		this.setLightOpacity(0);
		this.useNeighborBrightness = true;

		IBlockState state = this.blockState.getBaseState().withProperty(VARIANT, Variant.DEFAULT);
		if (!this.isDouble()) {
			state = state.withProperty(HALF, EnumBlockHalf.BOTTOM);
		}
		this.setDefaultState(state);

		ForgeRegistries.BLOCKS.register(this);
		BlockBase.rtBlockList.add(this);

		if (!this.isDouble()) {
			ItemSlab itemSlab = new ItemSlab(this, this, doubleSlabBlock);
			ForgeRegistries.ITEMS.register(itemSlab.setRegistryName(this.getRegistryName()));
		}
	}

	@Override
	public boolean isDouble() {
		return false;
	}

	@Override
	public IProperty<?> getVariantProperty() {
		return VARIANT;
	}

	@Override
	public Comparable<?> getTypeForItem(ItemStack stack) {
		return Variant.DEFAULT;
	}

	@Override
	public int damageDropped(IBlockState state) {
		return 0;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(ModBlocks.spectreSlab);
	}

	@Override
	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
		return new ItemStack(ModBlocks.spectreSlab);
	}

	@Override
	public String getTranslationKey(int meta) {
		return this.getTranslationKey();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		IBlockState state = this.getDefaultState().withProperty(VARIANT, Variant.DEFAULT);
		if (!this.isDouble()) {
			state = state.withProperty(HALF, (meta & 8) == 0 ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP);
		}
		return state;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int i = 0;
		if (!this.isDouble() && state.getValue(HALF) == EnumBlockHalf.TOP) {
			i |= 8;
		}
		return i;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return this.isDouble() ? new BlockStateContainer(this, VARIANT) : new BlockStateContainer(this, HALF, VARIANT);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos,
			EnumFacing side) {
		IBlockState neighborState = blockAccess.getBlockState(pos.offset(side));
		Block neighborBlock = neighborState.getBlock();

		if (side.getAxis().isHorizontal()) {
			// Hide internal faces between matching Spectre slab halves.
			if (neighborBlock instanceof BlockSpectrePlankSlab) {
				BlockSpectrePlankSlab thisSlab = (BlockSpectrePlankSlab) blockState.getBlock();
				BlockSpectrePlankSlab neighborSlab = (BlockSpectrePlankSlab) neighborBlock;

				if (thisSlab.isDouble() && neighborSlab.isDouble()) {
					return false;
				}

				if (!thisSlab.isDouble() && !neighborSlab.isDouble()
						&& blockState.getValue(HALF) == neighborState.getValue(HALF)) {
					return false;
				}
			}

			// Hide seam when slab and stair occupy the same vertical half.
			if (!this.isDouble() && neighborBlock instanceof BlockSpectrePlankStairs) {
				String slabHalf = blockState.getValue(HALF).getName();
				String stairHalf = neighborState.getValue(BlockStairs.HALF).getName();
				if (slabHalf.equals(stairHalf)) {
					return false;
				}
			}
		}

		return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return this.isDouble();
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return this.isDouble();
	}

	@Override
	public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public EnumPushReaction getPushReaction(IBlockState state) {
		return EnumPushReaction.NORMAL;
	}

	public enum Variant implements IStringSerializable {
		DEFAULT;

		@Override
		public String getName() {
			return "default";
		}
	}
}
