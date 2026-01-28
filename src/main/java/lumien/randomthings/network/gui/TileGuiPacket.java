package lumien.randomthings.network.gui;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import lumien.randomthings.network.ServerboundMessage;

public abstract class TileGuiPacket<T extends TileEntity> implements ServerboundMessage
{
    protected BlockPos pos;

    public TileGuiPacket() {}

    public TileGuiPacket(BlockPos pos)
    {
        this.pos = pos;
    }

    @Nonnull
    protected abstract Class<T> getTileType();

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        pos = buf.readBlockPos();
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        buf.writeBlockPos(pos);
    }

    @Override
    public final void handleOnServer(@Nonnull EntityPlayerMP player)
    {
        World world = player.world;
        if (!world.isBlockLoaded(pos) || !world.isBlockModifiable(player, pos)) return;

        Class<T> clazz = getTileType();
        TileEntity tile = world.getTileEntity(pos);
        if (clazz.isInstance(tile))
        {
            onPacket(player, clazz.cast(tile));
        }
    }

    protected abstract void onPacket(@Nonnull EntityPlayerMP player, T tile);
}
