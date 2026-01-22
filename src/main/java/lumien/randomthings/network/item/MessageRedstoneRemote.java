package lumien.randomthings.network.item;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import lumien.randomthings.container.inventories.InventoryItem;
import lumien.randomthings.handler.redstonesignal.RedstoneSignalHandler;
import lumien.randomthings.item.ItemPositionFilter;
import lumien.randomthings.item.ItemRedstoneRemote;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.network.ServerboundMessage;

public class MessageRedstoneRemote implements ServerboundMessage
{
	private EnumHand usingHand;
	private int slotUsed;

	public MessageRedstoneRemote() {}

	public MessageRedstoneRemote(EnumHand usingHand, int slotUsed)
	{
		this.usingHand = usingHand;
		this.slotUsed = slotUsed;
	}

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        usingHand = buf.readEnumValue(EnumHand.class);
        slotUsed = buf.readInt();
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        buf.writeEnumValue(usingHand);
        buf.writeInt(slotUsed);
    }

    @Override
    public void handleOnServer(@Nonnull EntityPlayerMP player)
    {
        if (slotUsed < 0 || slotUsed >= InventoryPlayer.getHotbarSize()) return;

        ItemStack using = player.getHeldItem(usingHand);
        if (!(using.getItem() instanceof ItemRedstoneRemote)) return;

        InventoryItem itemInventory = new InventoryItem("RedstoneRemote", 18, using);

        ItemStack positionFilter = itemInventory.getStackInSlot(slotUsed);

        if (positionFilter.getItem() == ModItems.positionFilter)
        {
            BlockPos target = ItemPositionFilter.getPosition(positionFilter);

            if (target != null)
            {
                RedstoneSignalHandler.getHandler().addSignal(player.world, target, 20, 15);
            }
        }
    }

    public static class Handler extends NoReplyHandler<MessageRedstoneRemote> {}
}
