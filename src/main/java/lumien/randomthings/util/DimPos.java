package lumien.randomthings.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import com.google.common.base.MoreObjects;

public class DimPos
{
    private final BlockPos pos;
    private final int dim;
    private WeakReference<World> worldRef;

    public DimPos(@Nonnull BlockPos pos, int dim)
    {
        this.pos = pos;
        this.dim = dim;
    }

    public static DimPos of(BlockPos pos, World world)
    {
        return of(pos, world.provider.getDimension());
    }

    public static DimPos of(BlockPos pos, int dimension)
    {
        return new DimPos(pos, dimension);
    }

    public BlockPos getPos()
    {
        return pos;
    }

    public int getDimension()
    {
        return dim;
    }

    public DimPos offset(@Nonnull EnumFacing side)
    {
        return new DimPos(pos.offset(side), dim);
    }

    @Nullable
    public World getWorld(boolean forceLoad)
    {
        if (worldRef == null)
        {
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            {
                World world = Minecraft.getMinecraft().world;
                if (world != null && world.provider.getDimension() == dim)
                {
                    worldRef = new WeakReference<>(world);
                }
                else
                {
                    return null;
                }
            }
            else
            {
                if (forceLoad)
                {
                    worldRef = new WeakReference<>(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dim));
                }
                else
                {
                    worldRef = new WeakReference<>(DimensionManager.getWorld(dim));
                }
            }
        }

        World world = worldRef.get();
        if (world == null)
        {
            if (forceLoad)
            {
                worldRef = new WeakReference<>(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dim));
            }
            else
            {
                worldRef = new WeakReference<>(DimensionManager.getWorld(dim));
            }
        }
        return world;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if (!(other instanceof DimPos))
        {
            return false;
        }

        DimPos dimPos = (DimPos) other;
        return dim == dimPos.dim && Objects.equals(pos, dimPos.pos);
    }

    @Override
    public int hashCode()
    {
        int result = pos.hashCode();
        result = 31 * result + dim;
        return result;
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("dim", dim)
                .add("x", pos.getX()).add("y", pos.getY()).add("z", pos.getZ())
                .add("hasWorldRef", worldRef != null && worldRef.get() != null)
                .toString();
    }
}
