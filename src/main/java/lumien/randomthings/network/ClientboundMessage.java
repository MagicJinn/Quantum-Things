package lumien.randomthings.network;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import com.google.common.base.Preconditions;

public interface ClientboundMessage extends IRTMessage
{
    /**
     * Handles this message on the main thread.
     *
     * @param player The client's player.
     */
    void handleOnClient(@Nonnull EntityPlayer player);

    /**
     * Call this from an {@link IMessageHandler} to handle the client-bound message.
     * By default, for thread safety, this will always schedule work to be done on the main thread.
     * Override this if you know you want to do work on the network thread.
     *
     * @param ctx The message context.
     */
    default void handleOnClient(MessageContext ctx)
    {
        Preconditions.checkArgument(ctx.side == Side.CLIENT);

        Minecraft.getMinecraft().addScheduledTask(() -> handleOnClient(Minecraft.getMinecraft().player));
    }

    abstract class NoReplyHandler<M extends ClientboundMessage> implements IMessageHandler<M, IMessage>
    {
        @Override
        public IMessage onMessage(M message, MessageContext ctx)
        {
            message.handleOnClient(ctx);
            return null;
        }
    }
}
