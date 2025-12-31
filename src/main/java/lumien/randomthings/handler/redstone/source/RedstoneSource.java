package lumien.randomthings.handler.redstone.source;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.BlockEvent;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import lumien.randomthings.handler.RTEventHandler;
import lumien.randomthings.item.ItemRedstoneActivator;
import lumien.randomthings.item.ItemRedstoneRemote;
import lumien.randomthings.tileentity.TileEntityRedstoneObserver;
import lumien.randomthings.tileentity.redstoneinterface.TileEntityRedstoneInterface;

public class RedstoneSource implements IDynamicRedstoneSource
{
    public static final String SOURCE_KEY = "source";
    public static final String TYPE_KEY = "type";
    public static final String ID_KEY = "id";
    public static final String POS_KEY = "pos";

    private Type type;
    private UUID id;
    private BlockPos pos;

    public RedstoneSource() {}

    public RedstoneSource(Type type, UUID id)
    {
        this.type = type;
        this.id = id;
        this.pos = null;
    }

    public RedstoneSource(Type type, UUID id, BlockPos pos)
    {
        this.type = type;
        this.id = id;
        this.pos = pos;
    }

    public RedstoneSource(IDynamicRedstoneSource source)
    {
        this(source.getType(), source.getId(), source.getSourcePos());
    }

    @Override
    public Type getType()
    {
        return type;
    }

    @Override
    public UUID getId()
    {
        return id;
    }

    @Nullable
    @Override
    public BlockPos getSourcePos()
    {
        return pos;
    }

    public static UUID getOrCreateId(ItemStack stack)
    {
        NBTTagCompound nbt;
        if (stack.hasTagCompound())
        {
            nbt = stack.getTagCompound();
        }
        else
        {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }
        Preconditions.checkNotNull(nbt);
        UUID sourceId;
        if (nbt.hasUniqueId(SOURCE_KEY))
        {
            sourceId = nbt.getUniqueId(SOURCE_KEY);
        }
        else
        {
            sourceId = UUID.randomUUID();
            nbt.setUniqueId(SOURCE_KEY, sourceId);
        }
        return sourceId;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        NBTTagCompound sourceData = new NBTTagCompound();

        sourceData.setByte(TYPE_KEY, (byte) type.index);
        sourceData.setUniqueId(ID_KEY, id);
        if (pos != null)
        {
            sourceData.setTag(POS_KEY, NBTUtil.createPosTag(pos));
        }

        compound.setTag(SOURCE_KEY, sourceData);
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        NBTTagCompound sourceData = compound.getCompoundTag(SOURCE_KEY);
        type = Type.byIndex(sourceData.getByte(TYPE_KEY));
        id = sourceData.getUniqueId(ID_KEY);
        if (sourceData.hasKey(POS_KEY, Constants.NBT.TAG_COMPOUND))
        {
            NBTTagCompound posCompound = sourceData.getCompoundTag(POS_KEY);
            pos = NBTUtil.getPosFromTag(posCompound);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof RedstoneSource)) return false;
        RedstoneSource that = (RedstoneSource) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(id);
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("id", id)
                .add("pos", pos)
                .toString();
    }

    public enum Type
    {
        /**
         * From {@link TileEntityRedstoneInterface}.
         */
        INTERFACE(0),
        /**
         * From {@link RTEventHandler#notifyNeighbors(BlockEvent.NeighborNotifyEvent)},
         * used with {@link TileEntityRedstoneObserver}.
         */
        OBSERVED(1),
        /**
         * From an item ({@link ItemRedstoneActivator}, {@link ItemRedstoneRemote}).
         */
        ITEM(2)
        ;

        private final int index;

        public static final Type[] VALUES = new Type[3];

        Type(int index)
        {
            this.index = index;
        }

        public int getIndex()
        {
            return index;
        }

        public static Type byIndex(int index)
        {
            return VALUES[MathHelper.abs(index % VALUES.length)];
        }

        static
        {
            for (Type value : values())
            {
                VALUES[value.index] = value;
            }
        }
    }
}
