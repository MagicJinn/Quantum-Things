package lumien.randomthings.block.spectretree;

import lumien.randomthings.block.BlockBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;

public class BlockSpectreLog extends BlockRotatedPillar
{
	public BlockSpectreLog()
	{
		super(Material.WOOD);

		this.setHardness(2.0F);
		this.setSoundType(SoundType.WOOD);

		this.setDefaultState(this.blockState.getBaseState().withProperty(AXIS, EnumFacing.Axis.Y));

		BlockBase.registerBlock("spectreLog", this);

		OreDictionary.registerOre("logWood", this);
	}

	/**
	 * Keep the historical meta encoding (0=X, 1=Y, 2=Z, 3=legacy NONE→Y)
	 * so existing worlds retain log rotation
	 */
	@Override
	public int getMetaFromState(IBlockState state)
	{
		switch (state.getValue(AXIS)) {
			case X:
				return 0;
			case Z:
				return 2;
			case Y:
			default:
				return 1;
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		EnumFacing.Axis axis;
		switch (meta) {
			case 0:
				axis = EnumFacing.Axis.X;
				break;
			case 2:
				axis = EnumFacing.Axis.Z;
				break;
			case 1:
			case 3: // legacy EnumAxis.NONE
			default:
				axis = EnumFacing.Axis.Y;
				break;
		}
		return this.getDefaultState().withProperty(AXIS, axis);
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, @Nonnull IBlockState state)
	{
		int i = 4;
		int j = i + 1;

		if (worldIn.isAreaLoaded(pos.add(-j, -j, -j), pos.add(j, j, j)))
		{
			for (BlockPos blockpos : BlockPos.getAllInBox(pos.add(-i, -i, -i), pos.add(i, i, i)))
			{
				IBlockState iblockstate = worldIn.getBlockState(blockpos);

				if (iblockstate.getBlock().isLeaves(iblockstate, worldIn, blockpos))
				{
					iblockstate.getBlock().beginLeavesDecay(iblockstate, worldIn, blockpos);
				}
			}
		}
	}

	/**
	 * Called by ItemBlocks just before a block is actually set in the world, to
	 * allow for adjustments to the IBlockstate
	 */
	@Override
	public IBlockState getStateForPlacement(@Nonnull World worldIn, @Nonnull BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer)
	{
		return this.getDefaultState().withProperty(AXIS, facing.getAxis());
	}

	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If
	 * inapplicable, returns the passed blockstate.
	 */
	@Override
	public IBlockState withRotation(@Nonnull IBlockState state, Rotation rot)
	{
		switch (rot)
		{
		case COUNTERCLOCKWISE_90:
		case CLOCKWISE_90:

			switch (state.getValue(AXIS))
			{
			case X:
				return state.withProperty(AXIS, EnumFacing.Axis.Z);
			case Z:
				return state.withProperty(AXIS, EnumFacing.Axis.X);
			default:
				return state;
			}

		default:
			return state;
		}
	}

	@Override
	public boolean canSustainLeaves(@Nonnull IBlockState state, @Nonnull net.minecraft.world.IBlockAccess world, @Nonnull BlockPos pos)
	{
		return true;
	}

	@Override
	public boolean isWood(@Nonnull net.minecraft.world.IBlockAccess world, @Nonnull BlockPos pos)
	{
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(@Nonnull IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, @Nonnull EnumFacing side)
	{
		IBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
		Block block = iblockstate.getBlock();

		if (blockState != iblockstate)
		{
			return true;
		}

		if (block == this)
		{
			return false;
		}

		return false;
	}

	@Override
	public boolean isOpaqueCube(@Nonnull IBlockState state)
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.TRANSLUCENT;
	}
}
