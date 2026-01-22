package lumien.randomthings.network.item;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import lumien.randomthings.item.ItemEnderLetter;
import lumien.randomthings.network.ServerboundMessage;

public class MessageEnderLetter implements ServerboundMessage
{
	private String receiver;

	public MessageEnderLetter() {}

	public MessageEnderLetter(String receiver)
	{
		this.receiver = receiver;
	}

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        this.receiver = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        ByteBufUtils.writeUTF8String(buf, receiver);
    }

    @Override
    public void handleOnServer(@Nonnull EntityPlayerMP player)
    {
        ItemStack equipped = player.getHeldItemMainhand();
        if (!(equipped.getItem() instanceof ItemEnderLetter)) return;

        NBTTagCompound nbt = equipped.getTagCompound();
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
            equipped.setTagCompound(nbt);
        }
        else if (nbt.getBoolean("received"))
        {
            return;
        }
        nbt.setString("receiver", receiver);
    }

    public static class Handler extends NoReplyHandler<MessageEnderLetter> {}
}
