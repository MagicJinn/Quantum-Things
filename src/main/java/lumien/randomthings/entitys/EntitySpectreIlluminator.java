package lumien.randomthings.entitys;

import javax.annotation.Nonnull;
import lumien.randomthings.handler.spectreilluminator.SpectreIlluminationHandler;
import lumien.randomthings.item.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class EntitySpectreIlluminator extends Entity
{
	int actionTimer;
	boolean illuminated;
	boolean inPosition;

	public EntitySpectreIlluminator(World worldIn)
	{
		super(worldIn);

		this.noClip = true;

		this.illuminated = false;

		this.inPosition = false;

		this.setSize(0.5F, 0.5F);
	}
	
	@Override
	public void onKillCommand()
	{
		super.onKillCommand();
		
		if (!world.isRemote)
		{
			SpectreIlluminationHandler handler = SpectreIlluminationHandler.get(this.world);

			BlockPos myPosition = this.getPosition();

			if (handler.isIlluminated(myPosition))
				handler.toggleChunk(this.world, myPosition);
		}
	}

	@Override
	public boolean isInRangeToRenderDist(double distance)
	{
		return true;
	}

	public EntitySpectreIlluminator(World worldIn, double x, double y, double z)
	{
		this(worldIn);

		this.setPosition(x, y, z);
	}

	@Override
	public boolean canBeCollidedWith()
	{
		return true;
	}

	@SuppressWarnings("null")
	@Override
	public EnumActionResult applyPlayerInteraction(@Nonnull EntityPlayer player, @Nonnull Vec3d vec,
			@Nonnull EnumHand hand)
	{
		if (!player.world.isRemote)
		{
			// Prevent duplication of items
			if (!isDead) {
				this.setDead();

				player.world.spawnEntity(new EntityItem(player.world, this.posX, this.posY,
						this.posZ, new ItemStack(ModItems.spectreIlluminator)));

				SpectreIlluminationHandler handler = SpectreIlluminationHandler.get(this.world);
				BlockPos pos = this.getPosition();
				if (handler.isIlluminated(pos)) {
					handler.toggleChunk(this.world, pos);
				}
		}
	}
		return EnumActionResult.SUCCESS;
	}

	@Override
	public void onEntityUpdate()
	{
		super.onEntityUpdate();

		long worldTick = this.world.getTotalWorldTime();
		int uuid = this.getEntityId(); // Use uuid as offset
		if (inPosition && worldTick % 20 + uuid != 0)
			return;

		if (!this.world.isRemote)
		{
			BlockPos myPosition = this.getPosition();

			Chunk thisChunk = world.getChunkFromBlockCoords(myPosition);

			// Efficient way to get the highest block in the chunk
			int heighestBlockInChunk = 0;
			for (int y : thisChunk.getHeightMap()) {
				if (y > heighestBlockInChunk) {
					heighestBlockInChunk = y;
				}
			}
			heighestBlockInChunk += 5; // 5 blocks above the ground

			ChunkPos pos = new ChunkPos(myPosition);

			double chunkX = (pos.getXStart() + pos.getXEnd()) / 2D;
			double chunkZ = (pos.getZStart() + pos.getZEnd()) / 2D;

			// Calculate distances
			double distX = chunkX - this.posX;
			double distY = heighestBlockInChunk - this.posY;
			double distZ = chunkZ - this.posZ;

			// Use smoother movement with interpolation
			double speed = 0.05;
			double threshold = 0.01;

			// X movement
			this.motionX = 0;
			if (Math.abs(distX) > threshold)
				this.motionX = Math.max(-speed, Math.min(speed, distX * 0.1));
			else
				this.posX = chunkX;


			// Y movement
			this.motionY = 0;
			if (Math.abs(distY) > threshold)
				this.motionY = Math.max(-speed, Math.min(speed, distY * 0.1));
			else
				this.posY = heighestBlockInChunk;

			// Z movement
			this.motionZ = 0;
			if (Math.abs(distZ) > threshold)
				this.motionZ = Math.max(-speed, Math.min(speed, distZ * 0.1));
			else
				this.posZ = chunkZ;


			if (Math.abs(distX) < threshold && Math.abs(distY) < threshold
					&& Math.abs(distZ) < threshold)
			{
				if (!illuminated
						&& !SpectreIlluminationHandler.get(this.world).isIlluminated(myPosition)) {
					SpectreIlluminationHandler.get(this.world).toggleChunk(this.world, myPosition);
					this.illuminated = true;
					inPosition = true;
				}
			}

		}
		if (motionX != 0 || motionY != 0 || motionZ != 0) {
			this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
		}
	}

	@Override
	protected void entityInit()
	{
	}

	@Override
	protected void readEntityFromNBT(@Nonnull NBTTagCompound compound)
	{
		this.illuminated = compound.getBoolean("illuminated");
	}

	@Override
	protected void writeEntityToNBT(@Nonnull NBTTagCompound compound)
	{
		compound.setBoolean("illuminated", illuminated);
	}

}
