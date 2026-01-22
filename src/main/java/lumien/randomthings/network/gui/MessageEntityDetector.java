package lumien.randomthings.network.gui;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import lumien.randomthings.tileentity.TileEntityEntityDetector;

public class MessageEntityDetector extends TileGuiPacket<TileEntityEntityDetector>
{
	private int buttonPressed;

	public MessageEntityDetector()
	{
        super();
	}

	public MessageEntityDetector(int buttonPressed, BlockPos pos)
	{
        super(pos);
		this.buttonPressed = buttonPressed;
	}

    @Nonnull
    @Override
    protected Class<TileEntityEntityDetector> getTileType()
    {
        return TileEntityEntityDetector.class;
    }

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        super.readPacketData(buf);
        buttonPressed = buf.readInt();
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        super.writePacketData(buf);
        buf.writeInt(buttonPressed);
    }

    @Override
    protected void onPacket(@Nonnull EntityPlayerMP player, TileEntityEntityDetector tile)
    {
        if (player.getDistanceSq(MessageEntityDetector.this.pos) >= 64) return;

        switch (buttonPressed)
        {
            case 0:
                tile.setRangeX(tile.getRangeX() - 1);
                break;
            case 1:
                tile.setRangeX(tile.getRangeX() + 1);
                break;

            case 2:
                tile.setRangeY(tile.getRangeY() - 1);
                break;
            case 3:
                tile.setRangeY(tile.getRangeY() + 1);
                break;

            case 4:
                tile.setRangeZ(tile.getRangeZ() - 1);
                break;
            case 5:
                tile.setRangeZ(tile.getRangeZ() + 1);
                break;

            case 6:
                tile.cycleFilter();
                break;

            case 7:
                tile.toggleInvert();
                break;
            case 8:
                tile.cyclePowerMode();
                break;
        }
    }

    public static class Handler extends NoReplyHandler<MessageEntityDetector> {}
}
