package lumien.randomthings.handler;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraft.world.DimensionType;
import net.minecraftforge.fml.common.FMLCommonHandler;
import lumien.randomthings.RandomThings;

/**
 * Reads world-level information from vanilla save data (e.g. level.dat).
 * No mod-specific persistence; does not extend WorldSavedData.
 */
public class RTWorldInformation
{
	/** Cache: once true it stays true for this run; avoids repeated NBT reads. */
	private static boolean dragonDefeatedCached = false;

	/**
	 * Reads whether the Ender Dragon has been defeated from vanilla level.dat:
	 * Data > DimensionData > The End > DragonFight > DragonKilled (byte)
	 */
	public static boolean isDragonDefeated()
	{
		// if the result is cached, return it
		if (dragonDefeatedCached)
			return true;

		WorldServer world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0);
		if (world == null)
			return false;

		NBTTagCompound endData = world.getWorldInfo().getDimensionData(DimensionType.THE_END.getId());
		if (endData == null || !endData.hasKey("DragonFight"))
			return false;

		// get the dragon fight data
		NBTTagCompound dragonFight = endData.getCompoundTag("DragonFight");
		// cache the result
		dragonDefeatedCached = dragonFight.getByte("DragonKilled") != 0;

		return dragonDefeatedCached;
	}
}
