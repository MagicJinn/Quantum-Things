package lumien.randomthings;

import java.util.List;

import lumien.randomthings.entitys.EntitySoul;
import lumien.randomthings.network.ClientboundMessage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class CommonProxy
{
	public void registerModels()
	{

	}

	public void renderRedstoneInterfaceStuff(float partialTicks)
	{

	}

	public void registerRenderers()
	{
	}

	public boolean canBeCollidedWith(EntitySoul soul)
	{
		return false;
	}

	public boolean isPlayerOnline(String username)
	{
		return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(username) != null;
	}

	public void scheduleColor(Object o)
	{

	}
	public void scheduleClientMessage(ClientboundMessage message)  
    {  
    
	}

	public void spawnFlooFlameParticles(World world, List<BlockPos> brickPositions) {
	}

	public void spawnFlooTokenParticles(World world, int dimension, double posX, double posY, double posZ) {
	}
}
