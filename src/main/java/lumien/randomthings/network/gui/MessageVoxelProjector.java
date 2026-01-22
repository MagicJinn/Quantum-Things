package lumien.randomthings.network.gui;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import lumien.randomthings.container.ContainerVoxelProjector;
import lumien.randomthings.tileentity.TileEntityVoxelProjector;

public class MessageVoxelProjector extends TileGuiPacket<TileEntityVoxelProjector>
{
	private enum VALUE
	{
		MODEL_ROTATION, MODEL, SCALE, ROTATION_SPEED, AMBIENT_LIGHT, RANDOMIZE;
	}

    private VALUE value;

	// Model Rotation
    private int newModelRotation;

	// Model
    private String newModel;

	// Scale
    private int newScale;

	// Rotation Speed
    private int newRotationSpeed;

	// Ambient Light
    private boolean newAmbientLight;

	// Randomize
    private boolean newRandomize;

	public MessageVoxelProjector()
	{
        super();
	}

	public MessageVoxelProjector(BlockPos pos)
	{
		super(pos);
	}

    @Nonnull
    @Override
    protected Class<TileEntityVoxelProjector> getTileType()
    {
        return TileEntityVoxelProjector.class;
    }

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        super.readPacketData(buf);
        value = buf.readEnumValue(VALUE.class);

        switch (value)
        {
            case MODEL_ROTATION:
                this.newModelRotation = buf.readVarInt();
                break;
            case MODEL:
                this.newModel = ByteBufUtils.readUTF8String(buf);
                break;
            case SCALE:
                this.newScale = buf.readVarInt();
                break;
            case ROTATION_SPEED:
                this.newRotationSpeed = buf.readVarInt();
                break;
            case AMBIENT_LIGHT:
                this.newAmbientLight = buf.readBoolean();
                break;
            case RANDOMIZE:
                this.newRandomize = buf.readBoolean();
                break;
            default:
                break;
        }
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        super.writePacketData(buf);
        buf.writeEnumValue(value);
        switch (value)
        {
            case MODEL_ROTATION:
                buf.writeVarInt(newModelRotation);
                break;
            case MODEL:
                ByteBufUtils.writeUTF8String(buf, newModel);
                break;
            case SCALE:
                buf.writeVarInt(newScale);
                break;
            case ROTATION_SPEED:
                buf.writeVarInt(newRotationSpeed);
                break;
            case AMBIENT_LIGHT:
                buf.writeBoolean(newAmbientLight);
                break;
            case RANDOMIZE:
                buf.writeBoolean(newRandomize);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPacket(@Nonnull EntityPlayerMP player, TileEntityVoxelProjector tile)
    {
        if (!(player.openContainer instanceof ContainerVoxelProjector)) return;

        switch (value)
        {
            case MODEL:
                if (newModel != null)
                {
                    tile.setModel(newModel);
                }
                break;
            case MODEL_ROTATION:
                if (newModelRotation >= 0 && newModelRotation < 361)
                {
                    tile.setModelRotation(newModelRotation);
                }
                break;
            case SCALE:
                if (newScale >= 1 && newScale < 21)
                {
                    tile.setScale(newScale);
                }
                break;
            case ROTATION_SPEED:
                if (newRotationSpeed >= 0 && newRotationSpeed < 41)
                {
                    tile.setRotationSpeed(newRotationSpeed);
                }
                break;
            case AMBIENT_LIGHT:
                tile.setAmbientLight(newAmbientLight);
                break;
            case RANDOMIZE:
                tile.setRandomize(newRandomize);
                break;
            default:
                break;
        }
    }

    public void setModelRotation(int newModelRotation)
    {
        this.value = VALUE.MODEL_ROTATION;
        this.newModelRotation = newModelRotation;
    }

    public void setModel(String newModel)
    {
        this.value = VALUE.MODEL;
        this.newModel = newModel;
    }

	public void setScale(int scale)
	{
        this.value = VALUE.SCALE;
		this.newScale = scale;
	}

	public void setRotationSpeed(int rotationSpeed)
	{
        this.value = VALUE.ROTATION_SPEED;
		this.newRotationSpeed = rotationSpeed;
	}

    public void setAmbientLight(boolean ambientLight)
    {
        this.value = VALUE.AMBIENT_LIGHT;
        this.newAmbientLight = ambientLight;
    }

	public void setRandomize(boolean randomize)
	{
        this.value = VALUE.RANDOMIZE;
		this.newRandomize = randomize;
	}

    public static class Handler extends NoReplyHandler<MessageVoxelProjector> {}
}
