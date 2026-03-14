package lumien.randomthings.network;  
  
import javax.annotation.Nonnull;  
  
import net.minecraft.entity.player.EntityPlayer;  
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;  
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;  
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;  
import net.minecraftforge.fml.relauncher.Side;  
  
import com.google.common.base.Preconditions;  
  
import lumien.randomthings.RandomThings;  
  
public interface ClientboundMessage extends IRTMessage  
{  
    void handleOnClient(@Nonnull EntityPlayer player);  
  
    abstract class NoReplyHandler<M extends ClientboundMessage> implements IMessageHandler<M, IMessage>  
    {  
        @Override  
        public IMessage onMessage(M message, MessageContext ctx)  
        {  
            Preconditions.checkArgument(ctx.side == Side.CLIENT);  
            RandomThings.proxy.scheduleClientMessage(message);  
            return null;  
        }  
    }  
}