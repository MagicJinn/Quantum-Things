package lumien.randomthings.network.magicavoxel;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import lumien.randomthings.client.gui.GuiVoxelProjector;
import lumien.randomthings.network.ClientboundMessage;

public class MessageModelList implements ClientboundMessage
{
	private final List<String> modelList;

	public MessageModelList()
	{
		modelList = new ArrayList<>();
	}

	public void addModel(String modelName)
	{
		this.modelList.add(modelName);
	}

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        int size = buf.readVarInt();

        for (int i = 0; i < size; i++)
        {
            modelList.add(ByteBufUtils.readUTF8String(buf));
        }
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        buf.writeVarInt(modelList.size());

        for (String modelName : modelList)
        {
            ByteBufUtils.writeUTF8String(buf, modelName);
        }
    }

    @Override
    public void handleOnClient(@Nonnull EntityPlayer player)
    {
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if (!(currentScreen instanceof GuiVoxelProjector)) return;

        ((GuiVoxelProjector) currentScreen).setModelList(modelList);
    }

    public static class Handler extends NoReplyHandler<MessageModelList> {}
}
