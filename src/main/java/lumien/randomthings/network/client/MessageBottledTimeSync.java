package lumien.randomthings.network.client;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

import lumien.randomthings.capability.bottledtime.IBottledTime;
import lumien.randomthings.network.ClientboundMessage;

public class MessageBottledTimeSync implements ClientboundMessage
{
	private long bottledTime;

	public MessageBottledTimeSync() {}

	public MessageBottledTimeSync(long bottledTime)
	{
		this.bottledTime = bottledTime;
	}

	@Override
	public void readPacketData(PacketBuffer buf)
	{
		bottledTime = buf.readLong();
	}

	@Override
	public void writePacketData(PacketBuffer buf)
	{
		buf.writeLong(bottledTime);
	}

	@Override
	public void handleOnClient(@Nonnull EntityPlayer player)
	{
		IBottledTime cap = player.getCapability(IBottledTime.CAPABILITY_BOTTLED_TIME, null);
		if (cap != null)
			cap.setBottledTime(bottledTime);
	}

	public static class Handler extends NoReplyHandler<MessageBottledTimeSync> {}
}
