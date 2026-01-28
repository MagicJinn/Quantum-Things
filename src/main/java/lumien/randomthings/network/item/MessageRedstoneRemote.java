package lumien.randomthings.network.item;

import javax.annotation.Nonnull;
import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import lumien.randomthings.capability.redstone.IDynamicRedstone;
import lumien.randomthings.capability.redstone.IDynamicRedstoneManager;
import lumien.randomthings.container.inventories.InventoryItem;
import lumien.randomthings.handler.redstone.signal.TemporarySignal;
import lumien.randomthings.handler.redstone.source.IDynamicRedstoneSource;
import lumien.randomthings.handler.redstone.source.RedstoneSource;
import lumien.randomthings.item.ItemPositionFilter;
import lumien.randomthings.item.ItemRedstoneRemote;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.network.ServerboundMessage;

import static lumien.randomthings.handler.redstone.source.RedstoneSource.Type.ITEM;

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
        slotUsed = buf.readVarInt();
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        buf.writeEnumValue(usingHand);
        buf.writeVarInt(slotUsed);
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
            if (target == null) return;

            World world = player.world;
            IDynamicRedstoneManager manager = world.getCapability(IDynamicRedstoneManager.CAPABILITY_DYNAMIC_REDSTONE, null);
            if (manager != null)
            {
                IDynamicRedstoneSource source = new RedstoneSource(ITEM, RedstoneSource.getOrCreateId(using));
                for (EnumFacing side : EnumFacing.VALUES)
                {
                    IDynamicRedstone signal = manager.getDynamicRedstone(target.offset(side), side, null, EnumSet.of(ITEM));
                    signal.setRedstoneLevel(new TemporarySignal(source, 15, 15, 20));
                }
            }
        }
    }

    public static class Handler extends NoReplyHandler<MessageRedstoneRemote> {}
}
