package lumien.randomthings.network.gui;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import lumien.randomthings.container.ContainerSoundRecorder;
import lumien.randomthings.network.ServerboundMessage;

public class MessageSelectSound implements ServerboundMessage
{
	private String selectedSound;

	public MessageSelectSound() {}

	public MessageSelectSound(String selectedSound)
	{
		this.selectedSound = selectedSound;
	}

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        selectedSound = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        ByteBufUtils.writeUTF8String(buf, selectedSound);
    }

    @Override
    public void handleOnServer(@Nonnull EntityPlayerMP player)
    {
        if (selectedSound == null) return;

        if (player.openContainer instanceof ContainerSoundRecorder)
        {
            ContainerSoundRecorder recorder = (ContainerSoundRecorder) player.openContainer;
            recorder.outputSound(selectedSound);
        }
    }

    public static class Handler extends NoReplyHandler<MessageSelectSound> {}
}
