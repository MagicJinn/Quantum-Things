package lumien.randomthings.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import static lumien.randomthings.block.BlockAncientBrick.VARIANT.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import lumien.randomthings.config.Features;
import lumien.randomthings.item.block.ItemBlockAncientBrick;
import lumien.randomthings.tileentity.TileEntityAncientFurnace;

public class BlockAncientBrick extends BlockBase
{
	public static enum VARIANT implements IStringSerializable
	{
		RUNES("runes"), DEFAULT("default"), STAR_EMPTY("empty"), STAR_FULL("full"), OUTPUT("output");

		String name;

		VARIANT(String name)
		{
			this.name = name;
		}

		@Override
		public String getName()
		{
			return name;
		}
	}

	public static PropertyEnum<VARIANT> TYPE = PropertyEnum.create("variant", BlockAncientBrick.VARIANT.class);

	protected BlockAncientBrick()
	{
		super("ancientBrick", Material.ROCK, ItemBlockAncientBrick.class);

		// this.setBlockUnbreakable().setResistance(6000000.0F);
		this.setTickRandomly(true);

		this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, VARIANT.RUNES));
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
	{
		if (!worldIn.isRemote && rand.nextInt(3) == 0)
		{
			IBlockState upState = worldIn.getBlockState(pos.up());

			if (upState.getBlock() == Blocks.SNOW_LAYER)
			{
				worldIn.setBlockToAir(pos.up());
			}
		}
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		if (Features.ANCIENT_BRICK_DROP_ITEMS) {
			List<ItemStack> drops = new ArrayList<>();
			// Full variant never drops as item; drop empty instead
			int meta = state.getValue(TYPE) == STAR_FULL ? STAR_EMPTY.ordinal() : getMetaFromState(state);
			drops.add(new ItemStack(this, 1, meta));
			return drops;
		}
		return Collections.emptyList();
	}

	@Override
	public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
		return Features.ANCIENT_BRICK_DROP_ITEMS ? 2.0F : -1.0F;
	}

	// Not great, but we want it to update immediately when the config is changed,
	// and setResistance() doesn't work for this
	@Override
	public float getExplosionResistance(Entity exploder) {
		return Features.ANCIENT_BRICK_DROP_ITEMS ? 2.0F : (this.blockResistance / 5.0F);
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
		return Features.ANCIENT_BRICK_DROP_ITEMS ? 2.0F : 6000000.0F;
	}

	@Override
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items)
	{
		for (VARIANT variant : VARIANT.values()) {
			items.add(new ItemStack(this, 1, variant.ordinal()));
		}
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, new IProperty[] { TYPE });
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		ItemStack held = playerIn.getHeldItem(hand);

		if (held.getItem() == Items.NETHER_STAR)
		{
			if (state.getValue(TYPE) == STAR_EMPTY)
			{
				if (!worldIn.isRemote)
				{
					worldIn.setBlockState(pos, state.withProperty(TYPE, STAR_FULL));

					IBlockState stateDown = worldIn.getBlockState(pos.down());

					if (stateDown.getBlock() == ModBlocks.ancientFurnace)
					{
						TileEntityAncientFurnace te = (TileEntityAncientFurnace) worldIn.getTileEntity(pos.down());

						te.start();
					}

				}

				return true;
			}
		}

		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return getDefaultState().withProperty(TYPE, VARIANT.values()[meta]);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return state.getValue(TYPE).ordinal();
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		// When filled, emit light
		return state.getValue(TYPE) == VARIANT.STAR_FULL ? 15 : 0;
	}
}
