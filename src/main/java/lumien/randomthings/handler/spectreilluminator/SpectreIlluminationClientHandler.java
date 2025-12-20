package lumien.randomthings.handler.spectreilluminator;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import lumien.randomthings.util.WorldUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;

public class SpectreIlluminationClientHandler
{
	static LongSet illuminatedChunks = new LongOpenHashSet();
	
	public static boolean isIlluminated(BlockPos pos)
	{
		return illuminatedChunks.contains(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
	}

	public static void loadChunk(Chunk chunk)
	{
		illuminatedChunks.remove(ChunkPos.asLong(chunk.x, chunk.z));
	}

	public static void setIlluminated(long chunkLong, boolean illuminated)
	{
		if (illuminated)
		{
			illuminatedChunks.add(chunkLong);
		}
		else
		{
			illuminatedChunks.remove(chunkLong);
		}
		
		SpectreIlluminationHelper.lightUpdateChunk(Minecraft.getMinecraft().world, WorldUtil.getChunkPosFromLong(chunkLong));
	}
}
