package lumien.randomthings.network.gui;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

import lumien.randomthings.container.ContainerTE;
import lumien.randomthings.network.ServerboundMessage;

public class MessageContainerSignal implements ServerboundMessage
{
	private int signal;

	public MessageContainerSignal() {}

	public MessageContainerSignal(int signal)
	{
		this.signal = signal;
	}

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        signal = buf.readVarInt();
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        buf.writeVarInt(signal);
    }

    @Override
    public void handleOnServer(@Nonnull EntityPlayerMP player)
    {
        if (!(player.openContainer instanceof ContainerTE)) return;

        ((ContainerTE<?>) player.openContainer).signal(signal);
    }

    public static class Handler extends NoReplyHandler<MessageContainerSignal> {}
}
