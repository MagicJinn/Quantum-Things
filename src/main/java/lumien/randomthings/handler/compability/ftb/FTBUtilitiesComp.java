package lumien.randomthings.handler.compability.ftb;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.logging.log4j.Level;

import lumien.randomthings.RandomThings;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.Loader;

/**
 * Soft dependency on FTB Utilities for AFK detection (Time in a Bottle).
 * FTB Utilities is closed source and does not provide a public API for AFK detection (as far as I can tell)
 */
public final class FTBUtilitiesComp
{
	public static final String MODID = "ftbutilities";

	private static final String FTB_NAMESPACE = "com.feed_the_beast";

	private static final String CLASS_UNIVERSE = FTB_NAMESPACE + ".ftblib.lib.data.Universe";
	private static final String CLASS_FORGE_PLAYER = FTB_NAMESPACE + ".ftblib.lib.data.ForgePlayer";
	private static final String CLASS_PLAYER_DATA = FTB_NAMESPACE + ".ftbutilities.data.FTBUtilitiesPlayerData";
	private static final String CLASS_CONFIG = FTB_NAMESPACE + ".ftbutilities.FTBUtilitiesConfig";

	private static final String METHOD_GET = "get";
	private static final String METHOD_GET_PLAYER = "getPlayer";
	private static final String METHOD_IS_ENABLED = "isEnabled";
	private static final String METHOD_GET_NOTIFICATION_TIMER = "getNotificationTimer";

	private static final String FIELD_AFK_TIME = "afkTime";
	private static final String FIELD_AFK = "afk";

	private static final String LOG_HOOK_FAILED = "Could not hook into FTB Utilities AFK API. Time in a Bottle AFK pause will be unavailable. Error: ";

	private static boolean lookupFailed;
	private static boolean lookupDone;

	private static Method universeGet;
	private static Method universeGetPlayer;
	private static Method playerDataGet;
	private static Field afkTimeField;
	private static Method afkIsEnabled;
	private static Method afkGetNotificationTimer;
	private static Object afkConfigInstance;

	private FTBUtilitiesComp()
	{
	}

	/**
	 * Whether FTB Utilities considers this player AFK (notification timer):
	 * {@code data.afkTime >= FTBUtilitiesConfig.afk.getNotificationTimer()}.
	 */
	public static boolean isPlayerAfk(EntityPlayer player)
	{
		if (player == null || player.world == null || player.world.isRemote || !Loader.isModLoaded(MODID))
			return false;

		if (!ensureLookup())
			return false;

		try
		{
			MinecraftServer server = player.getServer();
			if (server == null || !((Boolean) afkIsEnabled.invoke(afkConfigInstance, server)))
				return false;

			Object universe = universeGet.invoke(null);
			if (universe == null)
				return false;

			Object forgePlayer = universeGetPlayer.invoke(universe, player);
			if (forgePlayer == null)
				return false;

			Object data = playerDataGet.invoke(null, forgePlayer);
			if (data == null)
				return false;

			long afkTime = afkTimeField.getLong(data);
			long notificationTimer = (Long) afkGetNotificationTimer.invoke(afkConfigInstance);
			return afkTime >= notificationTimer;
		}
		catch (Exception e)
		{
			// Universe may not be loaded yet, or ranks may be unavailable. Fail open.
			return false;
		}
	}

	private static boolean ensureLookup()
	{
		if (lookupDone)
			return !lookupFailed;

		lookupDone = true;
		try
		{
			Class<?> universeClass = Class.forName(CLASS_UNIVERSE);
			universeGet = universeClass.getMethod(METHOD_GET);
			// FTBLib exposes getPlayer(ICommandSender), not EntityPlayer
			universeGetPlayer = universeClass.getMethod(METHOD_GET_PLAYER, ICommandSender.class);

			Class<?> playerDataClass = Class.forName(CLASS_PLAYER_DATA);
			Class<?> forgePlayerClass = Class.forName(CLASS_FORGE_PLAYER);
			playerDataGet = playerDataClass.getMethod(METHOD_GET, forgePlayerClass);
			afkTimeField = playerDataClass.getField(FIELD_AFK_TIME);

			Class<?> configClass = Class.forName(CLASS_CONFIG);
			afkConfigInstance = configClass.getField(FIELD_AFK).get(null);
			Class<?> afkClass = afkConfigInstance.getClass();
			afkIsEnabled = afkClass.getMethod(METHOD_IS_ENABLED, MinecraftServer.class);
			afkGetNotificationTimer = afkClass.getMethod(METHOD_GET_NOTIFICATION_TIMER);

			return true;
		}
		catch (Exception e)
		{
			lookupFailed = true;
			RandomThings.logger.log(Level.ERROR, LOG_HOOK_FAILED + e.getMessage());
			RandomThings.logger.log(Level.DEBUG, e);
			return false;
		}
	}
}
