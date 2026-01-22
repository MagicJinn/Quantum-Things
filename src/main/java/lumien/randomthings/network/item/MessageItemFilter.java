package lumien.randomthings.network.item;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

import lumien.randomthings.container.ContainerItemFilter;
import lumien.randomthings.network.ServerboundMessage;

public class MessageItemFilter implements ServerboundMessage
{
	private int buttonPressed;

	public MessageItemFilter() {}

	public MessageItemFilter(int buttonPressed)
	{
		this.buttonPressed = buttonPressed;
	}

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        buttonPressed = buf.readInt();
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        buf.writeInt(buttonPressed);
    }

    @Override
    public void handleOnServer(@Nonnull EntityPlayerMP player)
    {
        if (!(player.openContainer instanceof ContainerItemFilter)) return;

        ContainerItemFilter itemFilterContainer = (ContainerItemFilter) player.openContainer;
        switch (buttonPressed)
        {
            case 0:
                itemFilterContainer.repres.toggleMetadata();
                break;
            case 1:
                itemFilterContainer.repres.toggleOreDict();
                break;
            case 2:
                itemFilterContainer.repres.toggleNBT();
                break;
            case 3:
                itemFilterContainer.repres.toggleListType();
                break;
        }
    }

    public static class Handler extends NoReplyHandler<MessageItemFilter> {}
}
