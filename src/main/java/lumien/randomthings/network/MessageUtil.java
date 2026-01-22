package lumien.randomthings.network;

import net.minecraft.network.Packet;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessageUtil
{
	public static void sendToAllWatchingPos(World worldObj, BlockPos pos, IMessage message)
	{
		if (worldObj.isBlockLoaded(pos))
		{
			try
			{
				Chunk c = worldObj.getChunk(pos);

				PlayerChunkMap playerManager = ((WorldServer) worldObj).getPlayerChunkMap();

				PlayerChunkMapEntry playerInstance = playerManager.getEntry(c.x, c.z);
				if (playerInstance != null)
				{
					playerInstance.sendPacket(PacketHandler.INSTANCE.getPacketFrom(message));
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void sendToAllWatchingPos(World worldObj, BlockPos pos, Packet packet)
	{
		if (worldObj.isBlockLoaded(pos))
		{
			try
			{
				Chunk c = worldObj.getChunk(pos);

				PlayerChunkMap playerManager = ((WorldServer) worldObj).getPlayerChunkMap();

				PlayerChunkMapEntry playerInstance = playerManager.getEntry(c.x, c.z);
				if (playerInstance != null)
				{
					playerInstance.sendPacket(packet);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
