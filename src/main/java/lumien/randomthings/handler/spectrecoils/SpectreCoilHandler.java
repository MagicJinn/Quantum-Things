package lumien.randomthings.handler.spectrecoils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import java.util.UUID;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.energy.IEnergyStorage;
import lumien.randomthings.config.SpectreCoils;

public class SpectreCoilHandler extends WorldSavedData
{
	static final String ID = "rtSpectreCoilHandler";

	Map<UUID, Integer> coilEntries;

	public SpectreCoilHandler()
	{
		this(ID);
	}

	public SpectreCoilHandler(String id)
	{
		super(id);

		coilEntries = new HashMap<UUID, Integer>();
	}

	private int clampEnergy(int energy) {
		int maxEnergy = SpectreCoils.SPECTRE_ENERGY_INJECTOR_MAX_ENERGY;
		// Clamp energy to valid range (0 - maxEnergy) to fix corrupted/negative values
		if (energy < 0)
			return 0;
		if (energy > maxEnergy)
			return maxEnergy;
		return energy;
	}

	private int sharedReceiveEnergy(UUID owner, int maxReceive, boolean simulate) {
		int rawEnergy = coilEntries.containsKey(owner) ? coilEntries.get(owner) : 0;
		// Sanitize current energy to prevent overflow from corrupted values
		int currentEnergy = clampEnergy(rawEnergy);
		int maxEnergy = SpectreCoils.SPECTRE_ENERGY_INJECTOR_MAX_ENERGY;

		// Calculate available space safely to prevent integer overflow
		// Use long to avoid overflow when currentEnergy is large
		long availableSpaceLong = (long) maxEnergy - (long) currentEnergy;
		int availableSpace = availableSpaceLong > Integer.MAX_VALUE ? Integer.MAX_VALUE
				: (int) availableSpaceLong;

		// Ensure availableSpace is not negative
		if (availableSpace < 0)
			availableSpace = 0;

		// Clamp maxReceive to available space to prevent overflow
		int energyToReceive = Math.min(maxReceive, availableSpace);

		// Ensure we don't receive negative energy
		if (energyToReceive < 0)
			energyToReceive = 0;

		if (!simulate && energyToReceive > 0) {
			// Use long to prevent overflow during addition
			long newEnergyLong = (long) currentEnergy + (long) energyToReceive;
			int newEnergy = newEnergyLong > maxEnergy ? maxEnergy : (int) newEnergyLong;
			coilEntries.put(owner, clampEnergy(newEnergy));
		}

		return energyToReceive;
	}

	public IEnergyStorage getStorage(UUID owner)
	{
		return new IEnergyStorage()
		{
			@Override
			public int receiveEnergy(int maxReceive, boolean simulate)
			{
				return sharedReceiveEnergy(owner, maxReceive, simulate);
			}

			@Override
			public int getMaxEnergyStored()
			{
				return SpectreCoils.SPECTRE_ENERGY_INJECTOR_MAX_ENERGY;
			}

			@Override
			public int getEnergyStored()
			{
				int rawEnergy = coilEntries.containsKey(owner) ? coilEntries.get(owner) : 0;
				// Sanitize energy to prevent displaying negative/corrupted values
				return clampEnergy(rawEnergy);
			}

			@Override
			public int extractEnergy(int maxExtract, boolean simulate)
			{
				return 0;
			}

			@Override
			public boolean canReceive()
			{
				return true;
			}

			@Override
			public boolean canExtract()
			{
				return false;
			}
		};
	}

	public static SpectreCoilHandler get(World worldObj)
	{
		SpectreCoilHandler instance = (SpectreCoilHandler) worldObj.getMapStorage().getOrLoadData(SpectreCoilHandler.class, ID);
		if (instance == null)
		{
			instance = new SpectreCoilHandler();
			worldObj.getMapStorage().setData(ID, instance);
		}

		return instance;
	}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound nbt)
	{
		NBTTagList list = nbt.getTagList("coilEntries", 10);

		this.coilEntries.clear();

		for (int i = 0; i < list.tagCount(); i++)
		{
			NBTTagCompound compound = list.getCompoundTagAt(i);

			UUID uuid = UUID.fromString(compound.getString("uuid"));
			int energy = compound.getInteger("energy");

			// Sanitize energy when loading from NBT to fix corrupted saves
			energy = clampEnergy(energy);

			this.coilEntries.put(uuid, energy);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound)
	{
		NBTTagList list = new NBTTagList();

		for (Entry<UUID, Integer> entry : coilEntries.entrySet())
		{
			NBTTagCompound entryCompound = new NBTTagCompound();

			entryCompound.setString("uuid", entry.getKey().toString());
			// Sanitize energy when saving to NBT to prevent saving corrupted values
			entryCompound.setInteger("energy", clampEnergy(entry.getValue()));

			list.appendTag(entryCompound);
		}

		compound.setTag("coilEntries", list);

		return compound;
	}

	@Override
	public boolean isDirty()
	{
		return true;
	}

	public IEnergyStorage getStorageCoil(UUID owner)
	{
		return new IEnergyStorage()
		{
			@Override
			public int receiveEnergy(int maxReceive, boolean simulate)
			{
				return sharedReceiveEnergy(owner, maxReceive, simulate);
			}

			@Override
			public int getMaxEnergyStored()
			{
				return SpectreCoils.SPECTRE_ENERGY_INJECTOR_MAX_ENERGY;
			}

			@Override
			public int getEnergyStored()
			{
				int rawEnergy = coilEntries.containsKey(owner) ? coilEntries.get(owner) : 0;
				// Sanitize energy to prevent displaying negative/corrupted values
				return clampEnergy(rawEnergy);
			}

			@Override
			public int extractEnergy(int maxExtract, boolean simulate)
			{
				int rawEnergy = coilEntries.containsKey(owner) ? coilEntries.get(owner) : 0;
				// Sanitize current energy to prevent issues with corrupted values
				int currentEnergy = clampEnergy(rawEnergy);

				int newEnergy = Math.max(0, currentEnergy - maxExtract);

				if (!simulate)
					coilEntries.put(owner, clampEnergy(newEnergy));

				return currentEnergy - newEnergy;
			}

			@Override
			public boolean canReceive()
			{
				return true;
			}

			@Override
			public boolean canExtract()
			{
				return true;
			}
		};
	}
}
