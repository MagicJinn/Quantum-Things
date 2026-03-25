package lumien.randomthings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import lumien.randomthings.handler.festival.FestivalHandler;
import lumien.randomthings.handler.floo.FlooFireplace;
import lumien.randomthings.handler.floo.FlooNetworkHandler;
import lumien.randomthings.handler.spectreilluminator.SpectreIlluminationHandler;
import lumien.randomthings.item.ItemBiomeCrystal;
import lumien.randomthings.item.ItemPositionFilter;
import lumien.randomthings.item.ItemTimeInABottle;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.lib.IOpable;
import lumien.randomthings.network.PacketHandler;
import lumien.randomthings.network.client.MessageNotification;
import lumien.randomthings.tileentity.TileEntityBase;
import lumien.randomthings.worldgen.WorldGenAncientFurnace;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class RTCommand extends CommandBase
{
	private static final String COMMAND_ROOT = "rt";
	private static final String COMMAND_PREFIX = "/" + COMMAND_ROOT;
	private static final String SUB_GENERATE_BIOME_CRYSTAL_CHESTS = "generateBiomeCrystalChests";
	private static final String SUB_SET_BIOME_CRYSTAL = "setBiomeCrystal";
	private static final String SUB_TP_FILTER = "tpFilter";
	private static final String SUB_TEST_SLIME_SPAWN = "testSlimeSpawn";
	private static final String SUB_NOTIFY = "notify";
	private static final String SUB_FIREPLACES = "fireplaces";
	private static final String SUB_FESTIVAL = "festival";
	private static final String SUB_ANCIENT_FURNACE = "ancientFurnace";
	private static final String SUB_OP = "op";
	private static final String SUB_TI = "ti";
	private static final String SUB_TIME_IN_A_BOTTLE = "timeinabottle";

	private static final String TIB_MODE_ADD = "add";
	private static final String TIB_MODE_QUERY = "query";
	private static final String TIB_MODE_SET = "set";
	private static final String TIB_MODE_SUBTRACT = "subtract";

	@Override
	public String getName()
	{
		return COMMAND_ROOT;
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return COMMAND_PREFIX;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
	{
		if (args.length == 1)
		{
			return getListOfStringsMatchingLastWord(args,
					new String[] { SUB_GENERATE_BIOME_CRYSTAL_CHESTS, SUB_SET_BIOME_CRYSTAL, SUB_TP_FILTER,
							SUB_TEST_SLIME_SPAWN,
							SUB_NOTIFY, SUB_FIREPLACES, SUB_FESTIVAL, SUB_OP, SUB_TIME_IN_A_BOTTLE });
		}
		else
		{
			if (args[0].equals(SUB_SET_BIOME_CRYSTAL) && args.length == 2)
			{
				return getListOfStringsMatchingLastWord(args, ForgeRegistries.BIOMES.getKeys());
			}
			else if (args[0].equals(SUB_NOTIFY))
			{
				if (args.length == 4)
				{
					return getListOfStringsMatchingLastWord(args, Item.REGISTRY.getKeys());
				}
				else if (args.length == 5)
				{
					return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
				}
			}
			else if (args[0].equals(SUB_OP))
			{
				if (args.length > 1 && args.length <= 4)
				{
					return getTabCompletionCoordinate(args, 1, pos);
				}
			}
			else if (args[0].equals(SUB_TIME_IN_A_BOTTLE)) {
				if (args.length == 2) {
					return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
				} else if (args.length == 3) {
					return getListOfStringsMatchingLastWord(args,
							new String[] { TIB_MODE_ADD, TIB_MODE_QUERY, TIB_MODE_SET, TIB_MODE_SUBTRACT });
				} else if (args.length == 4
						&& (args[2].equals(TIB_MODE_ADD) || args[2].equals(TIB_MODE_SET)
								|| args[2].equals(TIB_MODE_SUBTRACT))) {
					return getListOfStringsMatchingLastWord(args, new String[] { "30s", "60s", "10m", "1h" });
				}
			}
		}
		return Collections.<String>emptyList();
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if (args.length == 0)
		{
			return;
		}

		if (args[0].equals(SUB_SET_BIOME_CRYSTAL))
		{
			if (args.length == 2 && sender instanceof EntityPlayer)
			{
				String biomeName = args[1];

				EntityPlayer player = (EntityPlayer) sender;

				ItemStack equipped = player.getHeldItemMainhand();

				if (equipped != null && equipped.getItem() instanceof ItemBiomeCrystal)
				{
					if (equipped.getTagCompound() == null)
					{
						equipped.setTagCompound(new NBTTagCompound());
					}

					equipped.getTagCompound().setString("biomeName", args[1]);
				}
			}
		}
		else if (args[0].equals(SUB_GENERATE_BIOME_CRYSTAL_CHESTS))
		{
			if (sender instanceof EntityPlayer)
			{
				List<ResourceLocation> biomeIds = new ArrayList<>(Biome.REGISTRY.getKeys());

				int modX = 0;

				while (!biomeIds.isEmpty())
				{
					sender.getEntityWorld().setBlockState(sender.getPosition().add(modX, 0, 0), Blocks.CHEST.getDefaultState());

					IInventory inventory = (IInventory) sender.getEntityWorld().getTileEntity(sender.getPosition().add(modX, 0, 0));
					for (int i = 0; i < 27; i++)
					{
						if (!biomeIds.isEmpty())
						{
							ResourceLocation next = biomeIds.remove(biomeIds.size() - 1);
							ItemStack crystal = new ItemStack(ModItems.biomeCrystal);
							crystal.setTagCompound(new NBTTagCompound());
							crystal.getTagCompound().setString("biomeName", next.toString());
							inventory.setInventorySlotContents(i, crystal);
						}
					}

					modX += 2;
				}
			}
		}
		else if (args[0].equals(SUB_TP_FILTER))
		{
			if (sender instanceof EntityPlayerMP)
			{
				EntityPlayerMP player = (EntityPlayerMP) sender;

				ItemStack held;
				if ((held = player.getHeldItemMainhand()) != null && held.getItem() == ModItems.positionFilter)
				{
					BlockPos pos = ItemPositionFilter.getPosition(held);

					player.connection.setPlayerLocation(pos.getX(), pos.getY() + 150, pos.getZ(), player.rotationYaw, player.rotationPitch);
				}
			}
		}
		else if (args[0].equals(SUB_TEST_SLIME_SPAWN))
		{
			BlockPos pos = sender.getPosition();
			World world = sender.getEntityWorld();

			if (pos != null && world != null)
			{
				EntitySlime slime = new EntitySlime(world);
				slime.setLocationAndAngles(pos.getX(), pos.getY(), pos.getZ(), 0F, 0F);
				sender.sendMessage(new TextComponentString(slime.getCanSpawnHere() + ""));
			}
		}
		else if (args[0].equals(SUB_NOTIFY) && args.length == 5)
		{
			String title = args[1];
			String body = args[2];
			String itemName = args[3];
			String player = args[4];

			EntityPlayerMP playerEntity = server.getPlayerList().getPlayerByUsername(player);

			ItemStack itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)));

			MessageNotification message = new MessageNotification(title, body, itemStack);
			PacketHandler.instance().sendTo(message, playerEntity);
		}
		else if (args[0].equals(SUB_FIREPLACES))
		{
			FlooNetworkHandler handler = FlooNetworkHandler.get(sender.getEntityWorld());

			List<FlooFireplace> firePlaces = handler.getFirePlaces();

			sender.sendMessage(new TextComponentString("Floo Fireplaces in Dimension " + sender.getEntityWorld().provider.getDimension()).setStyle(new Style().setUnderlined(true)));
			sender.sendMessage(new TextComponentString(""));

			for (FlooFireplace firePlace : firePlaces)
			{
				String name = firePlace.getName();
				UUID creator = firePlace.getCreatorUUID();
				String ownerName = null;

				if (creator != null)
				{
					GameProfile profile = server.getPlayerProfileCache().getProfileByUUID(creator);

					if (profile != null)
					{
						ownerName = profile.getName();
					}
				}

				BlockPos pos = firePlace.getLastKnownPosition();

				sender.sendMessage(new TextComponentString((name == null ? "<Unnamed>" : name) + " | " + String.format("%d %d %d", pos.getX(), pos.getY(), pos.getZ()) + (ownerName != null ? " | " + ownerName : "")));
			}
		}
		else if (args[0].equals(SUB_FESTIVAL))
		{
			FestivalHandler handler = FestivalHandler.get(sender.getEntityWorld());
			List<EntityVillager> villagerList = sender.getEntityWorld().getEntitiesWithinAABB(EntityVillager.class, new AxisAlignedBB(sender.getPosition()).grow(50));

			if (!villagerList.isEmpty())
			{
				EntityVillager villager = villagerList.get(0);

				int success = handler.addFestival(villager);

				if (success == 2)
				{
					sender.sendMessage(new TextComponentTranslation("command.festival.scheduled"));
				}
				else
				{
					sender.sendMessage(new TextComponentTranslation("command.festival.failed"));
				}
			}
			else
			{
				sender.sendMessage(new TextComponentTranslation("command.festival.novillager"));
			}
		}
		else if (args[0].equals(SUB_ANCIENT_FURNACE))
		{
			WorldGenAncientFurnace.pattern.place(sender.getEntityWorld(), sender.getPosition(), 3);
		}
		else if (args[0].equals(SUB_OP) && args.length == 4)
		{
			if (sender instanceof EntityPlayerMP && sender.canUseCommand(2, SUB_OP))
			{
				EntityPlayerMP player = (EntityPlayerMP) sender;

				BlockPos target = parseBlockPos(sender, args, 1, false);

				TileEntity teTarget = player.world.getTileEntity(target);

				if (teTarget instanceof TileEntityBase && teTarget instanceof IOpable)
				{
					boolean newValue = ((TileEntityBase) teTarget).toggleOp();

					sender.sendMessage(new TextComponentTranslation("rt.command.op.feedback", newValue + ""));
				}
				else
				{
					sender.sendMessage(new TextComponentTranslation("rt.command.op.error"));
				}
			}
		}
		else if (args[0].equals(SUB_TI))
		{
			SpectreIlluminationHandler.get(sender.getEntityWorld()).toggleChunk(sender.getEntityWorld(), sender.getPosition());
		}
		else if (args[0].equals(SUB_TIME_IN_A_BOTTLE)) {
			// Check if we have all arguments
			if (args.length >= 3) {
				EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(args[1]);
				if (target == null) {
					throw new CommandException("Player not found: " + args[1]);
				}

				String mode = args[2];
				long currentTicks = ItemTimeInABottle.getStoredTime(target);
				long maxTicks = 42949672940L;

				if (mode.equals(TIB_MODE_QUERY)) {
					sender.sendMessage(new TextComponentString(
							target.getName() + " has " + (currentTicks / 20L) + " seconds in Time in a Bottle."));
				} else if (mode.equals(TIB_MODE_ADD)) {
					if (args.length < 4)
						throw new CommandException("Usage: " + COMMAND_PREFIX + " " + SUB_TIME_IN_A_BOTTLE
								+ " <playername> " + TIB_MODE_ADD + " <time>");

					long seconds = parseTimeInBottleSeconds(args[3], 1);
					long newTicks = Math.min(maxTicks, currentTicks + seconds * 20L);
					long changed = (newTicks - currentTicks) / 20L;
					ItemTimeInABottle.setStoredTime(target, newTicks);

					sender.sendMessage(new TextComponentString("Added " + changed + " seconds to " + target.getName()
							+ " (now " + (newTicks / 20L) + " seconds)."));
				} else if (mode.equals(TIB_MODE_SET)){
					if (args.length < 4)
						throw new CommandException("Usage: " + COMMAND_PREFIX + " " + SUB_TIME_IN_A_BOTTLE
								+ " <playername> " + TIB_MODE_SET + " <time>");

						long seconds = parseTimeInBottleSeconds(args[3], 0);
						long newTicks = Math.min(maxTicks, seconds * 20L);
						ItemTimeInABottle.setStoredTime(target, newTicks);

						sender.sendMessage(new TextComponentString(
								"Set " + target.getName() + "'s Time in a Bottle to " + (newTicks / 20L)
										+ " seconds."));
					} else if (mode.equals(TIB_MODE_SUBTRACT)) {
						if (args.length < 4)
							throw new CommandException("Usage: " + COMMAND_PREFIX + " " + SUB_TIME_IN_A_BOTTLE
									+ " <playername> " + TIB_MODE_SUBTRACT + " <time>");

						long seconds = parseTimeInBottleSeconds(args[3], 1);
						long newTicks = Math.max(0L, currentTicks - seconds * 20L);
						long changed = (currentTicks - newTicks) / 20L;
						ItemTimeInABottle.setStoredTime(target, newTicks);
						sender.sendMessage(new TextComponentString("Subtracted " + changed + " seconds from "
								+ target.getName() + " (now " + (newTicks / 20L) + " seconds)."));
					} else {
						throw new CommandException("Unknown action: " + mode);
					}
			}
		}
	}

	private long parseTimeInBottleSeconds(String input, int minSeconds) throws CommandException {
		if (input == null || input.isEmpty()) {
			throw new CommandException("Invalid time value: " + input);
		}

		char lastChar = input.charAt(input.length() - 1);
		long multiplier = 1L;
		String numberPart = input;

		if (!Character.isDigit(lastChar)) {
			switch (Character.toLowerCase(lastChar)) {
				case 's':
					multiplier = 1L;
					break;
				case 'm':
					multiplier = 60L;
					break;
				case 'h':
					multiplier = 3600L;
					break;
				case 'd':
					multiplier = 86400L;
					break;
				default:
					throw new CommandException("Invalid time suffix '" + lastChar + "'. Use s, m, h or d.");
			}

			numberPart = input.substring(0, input.length() - 1);
		}

		if (numberPart.isEmpty()) {
			throw new CommandException("Invalid time value: " + input);
		}

		for (int i = 0; i < numberPart.length(); i++) {
			if (!Character.isDigit(numberPart.charAt(i))) {
				throw new CommandException("Invalid time value: " + input + ". Use one value, e.g. 30s, 5m, 2h, 1d.");
			}
		}

		long amount;
		try {
			amount = Long.parseLong(numberPart);
		} catch (NumberFormatException e) {
			throw new CommandException("Invalid time value: " + input);
		}

		long seconds = amount * multiplier;
		if (seconds < minSeconds) {
			throw new CommandException("Time value must be at least " + minSeconds + " seconds.");
		}

		return seconds;
	}
}
