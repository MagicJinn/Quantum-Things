package lumien.randomthings.network.client;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import lumien.randomthings.client.notifications.NotificationToast;
import lumien.randomthings.network.ClientboundMessage;

public class MessageNotification implements ClientboundMessage
{
	private String title;
	private String body;
	private ItemStack icon;

    public MessageNotification() {}

	public MessageNotification(String title, String body, ItemStack icon)
	{
		this.title = title;
		this.body = body;
		this.icon = icon;
	}

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        title = ByteBufUtils.readUTF8String(buf);
        body = ByteBufUtils.readUTF8String(buf);
        icon = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        ByteBufUtils.writeUTF8String(buf, title);
        ByteBufUtils.writeUTF8String(buf, body);
        ByteBufUtils.writeItemStack(buf, icon);
    }

    @Override
    public void handleOnClient(@Nonnull EntityPlayer player)
    {
        Minecraft.getMinecraft().getToastGui().add(new NotificationToast(title, body, icon));
    }

    public static class Handler extends NoReplyHandler<MessageNotification> {}
}
