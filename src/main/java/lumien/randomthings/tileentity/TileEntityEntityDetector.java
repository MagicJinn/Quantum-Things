package lumien.randomthings.tileentity;

import java.util.List;

import com.google.common.base.Predicate;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.lib.IEntityFilterItem;
import lumien.randomthings.util.InventoryUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityEntityDetector extends TileEntityBase implements ITickable
{
	boolean powered;
	int powerLevel; // Power level 0-15 based on entity count
	int entityCount; // Number of entities detected

	int rangeX = 5;
	int rangeY = 5;
	int rangeZ = 5;

	boolean invert;
	POWER_MODE powerMode = POWER_MODE.WEAK;

	static final int MAX_RANGE = 10;

	FILTER filter = FILTER.ALL;

	InventoryBasic filterInventory;

	public TileEntityEntityDetector()
	{
		filterInventory = new InventoryBasic("tile.entityDetector", false, 1);
	}

	public enum FILTER
	{
		ALL("all", Entity.class), LIVING("living", EntityLivingBase.class), ANIMAL("animal", IAnimals.class), MONSTER("monster", IMob.class), PLAYER("player", EntityPlayer.class), ITEMS("item", EntityItem.class), CUSTOM("custom", null);


		String languageKey;
		Class filterClass;

		private FILTER(String languageKey, Class filterClass)
		{
			this.languageKey = "gui.entityDetector.filter." + languageKey;
			this.filterClass = filterClass;
		}

		public String getLanguageKey()
		{
			return languageKey;
		}
	}

	public enum POWER_MODE {
		WEAK("weak"), STRONG("strong"), PROPORTIONAL("proportional");

		String languageKey;

		private POWER_MODE(String languageKey) {
			this.languageKey = "gui.entityDetector.powerMode." + languageKey;
		}

		public String getLanguageKey() {
			return languageKey;
		}
	}

	public boolean strongOutput()
	{
		return powerMode == POWER_MODE.STRONG;
	}

	public POWER_MODE getPowerMode() {
		return powerMode;
	}

	public IInventory getInventory()
	{
		return filterInventory;
	}

	@Override
	public void update()
	{
		if (!this.world.isRemote)
		{
			int oldPowerLevel = powerLevel;
			boolean newPowered = checkSupposedPowereredState();

			if (newPowered != powered || oldPowerLevel != powerLevel)
			{
				powered = newPowered;
				this.syncTE();
				notifyNeighborsOfPowerChange();
			}
		}
	}

	private void notifyNeighborsOfPowerChange() {
		// Always notify neighbors of this block's position
		this.world.notifyNeighborsOfStateChange(pos, ModBlocks.entityDetector, false);

		// STRONG mode can propagate power through solid blocks,
		// so also notify adjacent blocks (regardless of power level)
		for (EnumFacing facing : EnumFacing.VALUES) {
			this.world.notifyNeighborsOfStateChange(this.pos.offset(facing), ModBlocks.entityDetector, false);
		}
	}

	public void cycleFilter()
	{
		int index = filter.ordinal();

		index++;

		if (index < FILTER.values().length)
		{
			filter = FILTER.values()[index];
		}
		else
		{
			filter = FILTER.values()[0];
		}

		syncTE();
	}

	private boolean checkSupposedPowereredState()
	{
		List<Entity> entityList = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(this.pos, this.pos.add(1, 1, 1)).grow(rangeX, rangeY, rangeZ), new FilterPredicate(filter.filterClass, filterInventory.getStackInSlot(0)));
		entityCount = entityList != null ? entityList.size() : 0;

		// Determine if entities are detected
		boolean hasEntities = entityCount > 0;
		boolean shouldBePowered = !invert == hasEntities;

		powerLevel = 0;

		// Calculate power level based on power mode
		switch (powerMode) {
			case WEAK:
				// Weak mode: 1 power when powered, 0 when not
				if (shouldBePowered) {
					powerLevel = 1;
				}
				break;
			case STRONG:
				// Strong mode: 15 power when powered, 0 when not
				if (shouldBePowered) {
					powerLevel = 15;
				}
				break;
			case PROPORTIONAL:
				// Proportional mode: 1 power per entity (up to 15)
				if (invert) {
					// When inverted, power decreases with entity count
					powerLevel = Math.max(0, 15 - Math.min(15, entityCount));
				} else {
					// Normal: 1 power per entity, capped at 15
					powerLevel = Math.min(15, entityCount);
				}
				break;
		}

		return shouldBePowered;
	}

	@Override
	public void writeDataToNBT(NBTTagCompound compound, boolean sync)
	{
		compound.setBoolean("powered", powered);
		compound.setInteger("powerLevel", powerLevel);

		compound.setInteger("rangeX", rangeX);
		compound.setInteger("rangeY", rangeY);
		compound.setInteger("rangeZ", rangeZ);

		compound.setInteger("filter", filter.ordinal());

		compound.setBoolean("invert", invert);
		// Save power mode
		compound.setInteger("powerMode", powerMode.ordinal());

		NBTTagCompound inventoryCompound = new NBTTagCompound();
		InventoryUtil.writeInventoryToCompound(inventoryCompound, filterInventory);
		compound.setTag("inventory", inventoryCompound);
	}

	@Override
	public void readDataFromNBT(NBTTagCompound compound, boolean sync)
	{
		powered = compound.getBoolean("powered");
		powerLevel = compound.hasKey("powerLevel") ? compound.getInteger("powerLevel") : 0;

		rangeX = compound.getInteger("rangeX");
		rangeY = compound.getInteger("rangeY");
		rangeZ = compound.getInteger("rangeZ");

		filter = FILTER.values()[compound.getInteger("filter")];

		invert = compound.getBoolean("invert");

		// Read power mode with backward compatibility
		if (compound.hasKey("powerMode")) {
			int modeOrdinal = compound.getInteger("powerMode");
			if (modeOrdinal >= 0 && modeOrdinal < POWER_MODE.values().length) {
				powerMode = POWER_MODE.values()[modeOrdinal];
			} else {
				powerMode = POWER_MODE.WEAK;
			}
		} else if (compound.hasKey("strongOutput")) {
			// Migrate old strongOutput boolean to power mode
			boolean oldStrongOutput = compound.getBoolean("strongOutput");
			powerMode = oldStrongOutput ? POWER_MODE.STRONG : POWER_MODE.WEAK;
		} else {
			powerMode = POWER_MODE.WEAK;
		}

		NBTTagCompound inventoryCompound = compound.getCompoundTag("inventory");

		if (inventoryCompound != null)
		{
			InventoryUtil.readInventoryFromCompound(inventoryCompound, filterInventory);
		}
	}

	public boolean isPowered()
	{
		return powered;
	}

	public int getPowerLevel() {
		return powerLevel;
	}

	public int getRangeX()
	{
		return rangeX;
	}

	public void setRangeX(int rangeX)
	{
		this.rangeX = Math.max(0, Math.min(rangeX, MAX_RANGE));

		this.syncTE();
	}

	public int getRangeY()
	{
		return rangeY;
	}

	public void setRangeY(int rangeY)
	{
		this.rangeY = Math.max(0, Math.min(rangeY, MAX_RANGE));
		this.syncTE();
	}

	public int getRangeZ()
	{
		return rangeZ;
	}

	public void setRangeZ(int rangeZ)
	{
		this.rangeZ = Math.max(0, Math.min(rangeZ, MAX_RANGE));

		this.syncTE();
	}

	public void toggleInvert()
	{
		invert = !invert;

		this.syncTE();
	}

	public boolean invert()
	{
		return invert;
	}

	public FILTER getFilter()
	{
		return filter;
	}

	private class FilterPredicate implements Predicate<Entity>
	{
		Class filterClass;
		ItemStack filterItem;
		IEntityFilterItem filterInstance;

		public FilterPredicate(Class filterClass, ItemStack filterItem)
		{
			this.filterClass = filterClass;
			this.filterItem = filterItem;

			if (!filterItem.isEmpty() && filterItem.getItem() instanceof IEntityFilterItem)
			{
				this.filterInstance = (IEntityFilterItem) filterItem.getItem();
			}
		}

		@Override
		public boolean apply(Entity input)
		{
			if (filterClass == null && filterInstance == null)
			{
				return false;
			}

			return filterClass == null ? filterInstance.apply(filterItem, input) : filterClass.isAssignableFrom(input.getClass());
		}

	}

	public void cyclePowerMode() {
		int index = powerMode.ordinal();
		index++;

		if (index < POWER_MODE.values().length) {
			powerMode = POWER_MODE.values()[index];
		} else {
			powerMode = POWER_MODE.values()[0];
		}

		// Recalculate power level with the new mode
		int oldPowerLevel = powerLevel;
		boolean oldPowered = powered;
		checkSupposedPowereredState();

		this.syncTE();

		// Only notify neighbors if the power level actually changed
		if (oldPowered != powered || oldPowerLevel != powerLevel) {
			notifyNeighborsOfPowerChange();
		}
	}

	// Backward compatibility method
	@Deprecated
	public void toggleStrongOutput()
	{
		// Cycle between WEAK and STRONG for backward compatibility
		if (powerMode == POWER_MODE.WEAK) {
			powerMode = POWER_MODE.STRONG;
		} else if (powerMode == POWER_MODE.STRONG) {
			powerMode = POWER_MODE.WEAK;
		} else {
			powerMode = POWER_MODE.STRONG;
		}

		// Recalculate power level with the new mode
		int oldPowerLevel = powerLevel;
		boolean oldPowered = powered;
		checkSupposedPowereredState();

		this.syncTE();

		// Only notify neighbors if the power level actually changed
		if (oldPowered != powered || oldPowerLevel != powerLevel) {
			notifyNeighborsOfPowerChange();
		}
	}
}
