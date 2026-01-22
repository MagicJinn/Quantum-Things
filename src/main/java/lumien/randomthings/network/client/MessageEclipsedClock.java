package lumien.randomthings.network.client;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

import com.google.common.base.Preconditions;
import lumien.randomthings.entitys.EntityEclipsedClock;
import lumien.randomthings.network.ClientboundMessage;

public class MessageEclipsedClock implements ClientboundMessage
{
	private int entityID;

	public MessageEclipsedClock() {}

	public MessageEclipsedClock(int entityID)
	{
		this.entityID = entityID;
	}

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        entityID = buf.readInt();
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        buf.writeInt(entityID);
    }

    @Override
    public void handleOnClient(@Nonnull EntityPlayer player)
    {
        World world = player.world;
        Preconditions.checkNotNull(world);

        Entity entity = world.getEntityByID(entityID);
        if (entity instanceof EntityEclipsedClock)
        {
            EntityEclipsedClock clock = (EntityEclipsedClock) entity;
            clock.triggerAnimation();
        }
    }

    public static class Handler extends NoReplyHandler<MessageEclipsedClock> {}
}
