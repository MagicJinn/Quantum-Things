package lumien.randomthings.tileentity.redstoneinterface;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import com.google.common.collect.Sets;
import lumien.randomthings.item.ItemPositionFilter;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.util.InventoryUtil;
import lumien.randomthings.util.NBTUtil;

public class TileEntityAdvancedRedstoneInterface extends TileEntityRedstoneInterface implements IInventoryChangedListener
{
    private Set<BlockPos> targets;
    private final InventoryBasic positionInventory;

	public TileEntityAdvancedRedstoneInterface()
	{
		targets = new HashSet<>();
        positionInventory = new InventoryBasic("Advanced Redstone Interface", false, 9);
        positionInventory.addInventoryChangeListener(this);
	}

	@Override
	public void writeDataToNBT(NBTTagCompound compound, boolean sync)
	{
		NBTTagList nbtTargetList = new NBTTagList();

        for (BlockPos pos : targets)
        {
            NBTTagCompound targetCompound = new NBTTagCompound();

            NBTUtil.writeBlockPosToNBT(targetCompound, "target", pos);
            nbtTargetList.appendTag(targetCompound);
        }

		compound.setTag("targets", nbtTargetList);

		NBTTagCompound inventoryCompound = new NBTTagCompound();
		InventoryUtil.writeInventoryToCompound(inventoryCompound, positionInventory);
		compound.setTag("inventory", inventoryCompound);
	}

	@Override
	public void readDataFromNBT(NBTTagCompound compound, boolean sync)
	{
		NBTTagList nbtTargetList = compound.getTagList("targets", 10);

        for (int i = 0; i < nbtTargetList.tagCount(); i++)
        {
            NBTTagCompound targetCompound = nbtTargetList.getCompoundTagAt(i);

            this.targets.add(NBTUtil.readBlockPosFromNBT(targetCompound, "target"));
        }

        NBTTagCompound inventoryCompound = compound.getCompoundTag("inventory");

        InventoryUtil.readInventoryFromCompound(inventoryCompound, positionInventory);
    }

    @Override
    public Set<BlockPos> getTargets()
    {
        return targets;
    }

	@Override
	public void onInventoryChanged(@Nonnull IInventory inventory)
	{
        if (world == null || world.isRemote || pos == null) return;

        HashSet<BlockPos> newTargets = new HashSet<>();

        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            ItemStack stack = inventory.getStackInSlot(i);

            BlockPos target;
            if (!stack.isEmpty() && stack.getItem() == ModItems.positionFilter && (target = ItemPositionFilter.getPosition(stack)) != null)
            {
                newTargets.add(target);
            }
        }
        Set<BlockPos> discardedPositions = Sets.difference(targets, newTargets);
        Set<BlockPos> changedPositions = Sets.difference(newTargets, targets);

        targets = newTargets;

        IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);

        invalidateTargets(discardedPositions);

        for (EnumFacing side : EnumFacing.VALUES)
        {
            sendSignal(side, changedPositions);
        }
    }

	public IInventory getTargetInventory()
	{
		return positionInventory;
	}
}
