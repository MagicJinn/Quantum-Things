package lumien.randomthings.network.item;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;

import lumien.randomthings.container.ContainerChunkAnalyzer;
import lumien.randomthings.item.ItemChunkAnalyzer;
import lumien.randomthings.network.ServerboundMessage;

public class MessageChunkAnalyzer implements ServerboundMessage
{
	public enum ACTION
	{
		START, STOP;
	}

    private ACTION action;
	
	public MessageChunkAnalyzer() {}
	
	public MessageChunkAnalyzer(ACTION action)
	{
		this.action = action;
	}

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        action = buf.readEnumValue(ACTION.class);
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        buf.writeEnumValue(action);
    }

    @Override
    public void handleOnServer(@Nonnull EntityPlayerMP player)
    {
        if (!(player.openContainer instanceof ContainerChunkAnalyzer)) return;

        ItemStack held = player.getHeldItemMainhand();
        if (!(held.getItem() instanceof ItemChunkAnalyzer)) return;

        NBTTagCompound nbt = held.getTagCompound();
        if (nbt != null && nbt.hasKey("result", Constants.NBT.TAG_COMPOUND))
        {
            nbt.removeTag("result");
            player.inventoryContainer.detectAndSendChanges();
        }

        ((ContainerChunkAnalyzer) player.openContainer).startScanning();
    }

    public static class Handler extends NoReplyHandler<MessageChunkAnalyzer> {}
}
