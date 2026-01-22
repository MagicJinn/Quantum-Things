package lumien.randomthings.network.item;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import lumien.randomthings.item.ItemSoundRecorder;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.network.ServerboundMessage;

public class MessagePlayedSound implements ServerboundMessage
{
	private String soundName;
	private int recorderSlot;

	public MessagePlayedSound() {}

	public MessagePlayedSound(String soundName, int recorderSlot)
	{
		this.soundName = soundName;
		this.recorderSlot = recorderSlot;
	}

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        soundName = ByteBufUtils.readUTF8String(buf);
        recorderSlot = buf.readInt();
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        ByteBufUtils.writeUTF8String(buf, soundName);
        buf.writeInt(recorderSlot);
    }

    @Override
    public void handleOnServer(@Nonnull EntityPlayerMP player)
    {
        if (recorderSlot < 0 || recorderSlot >= player.inventory.getSizeInventory()) return;

        ItemStack recorderStack = player.inventory.getStackInSlot(recorderSlot);
        if (recorderStack.getItem() == ModItems.soundRecorder)
        {
            ItemSoundRecorder.recordSound(recorderStack, soundName);
        }
    }

    public static class Handler extends NoReplyHandler<MessagePlayedSound> {}
}
