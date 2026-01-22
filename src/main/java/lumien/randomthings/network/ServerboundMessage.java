package lumien.randomthings.network;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import com.google.common.base.Preconditions;

public interface ServerboundMessage extends IRTMessage
{
    /**
     * Handles this message on the main thread.
     *
     * @param player The server-side player.
     */
    void handleOnServer(@Nonnull EntityPlayerMP player);

    /**
     * Call this from an {@link IMessageHandler} to handle the server-bound message.
     * By default, for thread safety, this will always schedule work to be done on the main thread.
     * Override this if you know you want to do work on the network thread.
     *
     * @param ctx The message context.
     */
    default void handleOnServer(MessageContext ctx)
    {
        Preconditions.checkArgument(ctx.side == Side.SERVER);

        FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handleOnServer(ctx.getServerHandler().player));
    }

    abstract class NoReplyHandler<M extends ServerboundMessage> implements IMessageHandler<M, IMessage>
    {
        @Override
        public IMessage onMessage(M message, MessageContext ctx)
        {
            message.handleOnServer(ctx);
            return null;
        }
    }
}
