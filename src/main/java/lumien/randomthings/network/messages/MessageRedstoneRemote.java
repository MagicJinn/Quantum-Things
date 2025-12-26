package lumien.randomthings.network.messages;

import java.util.EnumSet;

import io.netty.buffer.ByteBuf;
import lumien.randomthings.capability.redstone.IDynamicRedstone;
import lumien.randomthings.capability.redstone.IDynamicRedstoneManager;
import lumien.randomthings.container.inventories.InventoryItem;
import lumien.randomthings.handler.redstone.signal.TemporarySignal;
import lumien.randomthings.item.ItemPositionFilter;
import lumien.randomthings.item.ItemRedstoneRemote;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.network.IRTMessage;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import static lumien.randomthings.capability.redstone.IDynamicRedstone.Source.ITEM;

public class MessageRedstoneRemote implements IRTMessage
{
	EnumHand usingHand;
	int slotUsed;

	public MessageRedstoneRemote()
	{

	}

	public MessageRedstoneRemote(EnumHand usingHand, int slotUsed)
	{
		this.usingHand = usingHand;
		this.slotUsed = slotUsed;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.usingHand = EnumHand.values()[buf.readInt()];
		this.slotUsed = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(usingHand.ordinal());
		buf.writeInt(slotUsed);
	}

	@Override
	public void onMessage(MessageContext context)
	{
		EntityPlayerMP player = context.getServerHandler().player;

		if (slotUsed >= 0 && slotUsed < 9 && player != null)
		{
			ItemStack using = player.getHeldItem(usingHand);

			if (using != null && using.getItem() instanceof ItemRedstoneRemote)
			{
				InventoryItem itemInventory = new InventoryItem("RedstoneRemote", 18, using);

				ItemStack positionFilter = itemInventory.getStackInSlot(slotUsed);

				if (positionFilter != null && positionFilter.getItem() == ModItems.positionFilter)
				{
					BlockPos target = ItemPositionFilter.getPosition(positionFilter);

					if (target != null)
					{
                        World world = player.world;
                        IDynamicRedstoneManager manager = world.getCapability(IDynamicRedstoneManager.CAPABILITY_DYNAMIC_REDSTONE, null);
                        if (manager != null)
                        {
                            for (EnumFacing side : EnumFacing.VALUES)
                            {
                                IDynamicRedstone signal = manager.getDynamicRedstone(target.offset(side), side, EnumSet.of(ITEM));
                                signal.setRedstoneLevel(new TemporarySignal(15, 20, ITEM), true);
                            }
                        }
					}
				}
			}
		}
	}

	@Override
	public Side getHandlingSide()
	{
		return Side.SERVER;
	}

}
