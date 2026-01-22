package lumien.randomthings.network;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import lumien.randomthings.lib.Reference;
import lumien.randomthings.network.client.MessageEclipsedClock;
import lumien.randomthings.network.client.MessageNotification;
import lumien.randomthings.network.client.MessageSetBiome;
import lumien.randomthings.network.client.MessageSpectreIllumination;
import lumien.randomthings.network.gui.MessageAdvancedItemCollector;
import lumien.randomthings.network.gui.MessageAnalogEmitter;
import lumien.randomthings.network.gui.MessageChatDetector;
import lumien.randomthings.network.gui.MessageContainerSignal;
import lumien.randomthings.network.gui.MessageEntityDetector;
import lumien.randomthings.network.gui.MessageGlobalChatDetector;
import lumien.randomthings.network.gui.MessageNotificationInterface;
import lumien.randomthings.network.gui.MessageOnlineDetector;
import lumien.randomthings.network.gui.MessageSelectSound;
import lumien.randomthings.network.gui.MessageVoxelProjector;
import lumien.randomthings.network.item.MessageChunkAnalyzer;
import lumien.randomthings.network.item.MessageEnderLetter;
import lumien.randomthings.network.item.MessageItemFilter;
import lumien.randomthings.network.item.MessagePlayedSound;
import lumien.randomthings.network.item.MessageRedstoneRemote;
import lumien.randomthings.network.magicavoxel.MessageModelData;
import lumien.randomthings.network.magicavoxel.MessageModelList;
import lumien.randomthings.network.magicavoxel.MessageModelRequest;
import lumien.randomthings.network.magicavoxel.MessageModelRequestUpdate;
import lumien.randomthings.network.particle.MessageFlooParticles;
import lumien.randomthings.network.particle.MessageFlooToken;
import lumien.randomthings.network.render.MessageLightRedirector;
import lumien.randomthings.network.render.MessagePotionVaporizerParticles;
import lumien.randomthings.network.tile.MessageBiomeRadarAntenna;

public class PacketHandler
{
	public static SimpleNetworkWrapper INSTANCE = null;
    private static int packetId = 0;

	public static void init()
	{
        INSTANCE = new SimpleNetworkWrapper(Reference.MOD_ID.toLowerCase());

        // Client -> Server
		serverBound(new MessageOnlineDetector.Handler(), MessageOnlineDetector.class);
		serverBound(new MessageChatDetector.Handler(), MessageChatDetector.class);
		serverBound(new MessageAnalogEmitter.Handler(), MessageAnalogEmitter.class);
		serverBound(new MessageEnderLetter.Handler(), MessageEnderLetter.class);
		serverBound(new MessageEntityDetector.Handler(), MessageEntityDetector.class);
        serverBound(new MessageVoxelProjector.Handler(), MessageVoxelProjector.class);
		serverBound(new MessageAdvancedItemCollector.Handler(), MessageAdvancedItemCollector.class);
		serverBound(new MessageItemFilter.Handler(), MessageItemFilter.class);
        serverBound(new MessageRedstoneRemote.Handler(), MessageRedstoneRemote.class);
        serverBound(new MessageModelRequest.Handler(), MessageModelRequest.class);
        serverBound(new MessageContainerSignal.Handler(), MessageContainerSignal.class);
        serverBound(new MessageNotificationInterface.Handler(), MessageNotificationInterface.class);
        serverBound(new MessageGlobalChatDetector.Handler(), MessageGlobalChatDetector.class);
        serverBound(new MessagePlayedSound.Handler(), MessagePlayedSound.class);
        serverBound(new MessageSelectSound.Handler(), MessageSelectSound.class);
        serverBound(new MessageChunkAnalyzer.Handler(), MessageChunkAnalyzer.class);

        // Server -> Client
        clientBound(new MessagePotionVaporizerParticles.Handler(), MessagePotionVaporizerParticles.class);
		clientBound(new MessageLightRedirector.Handler(), MessageLightRedirector.class);
        clientBound(new MessageModelData.Handler(), MessageModelData.class);
        clientBound(new MessageModelList.Handler(), MessageModelList.class);
		clientBound(new MessageModelRequestUpdate.Handler(), MessageModelRequestUpdate.class);
		clientBound(new MessageBiomeRadarAntenna.Handler(), MessageBiomeRadarAntenna.class);
		clientBound(new MessageSetBiome.Handler(), MessageSetBiome.class);
		clientBound(new MessageNotification.Handler(), MessageNotification.class);
		clientBound(new MessageFlooParticles.Handler(), MessageFlooParticles.class);
		clientBound(new MessageFlooToken.Handler(), MessageFlooToken.class);
		clientBound(new MessageSpectreIllumination.Handler(), MessageSpectreIllumination.class);
		clientBound(new MessageEclipsedClock.Handler(), MessageEclipsedClock.class);
	}

    public static <M extends ClientboundMessage, R extends IMessage> void clientBound(IMessageHandler<M, R> handler, Class<M> messageClass)
    {
        INSTANCE.registerMessage(handler, messageClass, nextId(), Side.CLIENT);
    }

    public static <M extends ServerboundMessage, R extends IMessage> void serverBound(IMessageHandler<M, R> handler, Class<M> messageClass)
    {
        INSTANCE.registerMessage(handler, messageClass, nextId(), Side.SERVER);
    }

    public static int nextId()
    {
        return packetId++;
    }
}
