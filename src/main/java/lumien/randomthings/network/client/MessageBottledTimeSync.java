package lumien.randomthings.network.client;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

import lumien.randomthings.capability.bottledtime.IBottledTime;
import lumien.randomthings.network.ClientboundMessage;

public class MessageBottledTimeSync implements ClientboundMessage
{
	private long bottledTime;
	private boolean pausedDueToAfk;

	public MessageBottledTimeSync() {}

	public MessageBottledTimeSync(long bottledTime, boolean pausedDueToAfk)
	{
		this.bottledTime = bottledTime;
		this.pausedDueToAfk = pausedDueToAfk;
	}

	@Override
	public void readPacketData(PacketBuffer buf)
	{
		bottledTime = buf.readLong();
		pausedDueToAfk = buf.readBoolean();
	}

	@Override
	public void writePacketData(PacketBuffer buf)
	{
		buf.writeLong(bottledTime);
		buf.writeBoolean(pausedDueToAfk);
	}

	@Override
	public void handleOnClient(@Nonnull EntityPlayer player)
	{
		IBottledTime cap = player.getCapability(IBottledTime.CAPABILITY_BOTTLED_TIME, null);
		if (cap != null) {
			cap.setBottledTime(bottledTime);
			cap.setPausedDueToAfk(pausedDueToAfk);
		}
	}

	public static class Handler extends NoReplyHandler<MessageBottledTimeSync> {}
}
