package lumien.randomthings.network;

import net.minecraft.network.Packet;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessageUtil
{
	public static void sendToAllWatchingPos(World world, BlockPos pos, IMessage message)
	{
		if (!world.isBlockLoaded(pos)) return;

        PacketHandler.instance().sendToAllTracking(message, new NetworkRegistry.TargetPoint(world.provider.getDimension(),
                pos.getX(), pos.getY(), pos.getZ(), 0));
	}

	public static void sendToAllWatchingPos(World world, BlockPos pos, Packet<?> packet)
	{
		if (!world.isBlockLoaded(pos)) return;

        try
        {
            Chunk c = world.getChunk(pos);

            PlayerChunkMap playerManager = ((WorldServer) world).getPlayerChunkMap();

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
