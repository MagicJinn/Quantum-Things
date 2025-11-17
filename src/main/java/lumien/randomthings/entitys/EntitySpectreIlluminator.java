package lumien.randomthings.entitys;

import javax.annotation.Nonnull;
import lumien.randomthings.RandomThings;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class EntitySpectreIlluminator extends Entity
{
	boolean illuminated;

	double targetX = -1;
	int targetY;
	double targetZ = -1;

	public EntitySpectreIlluminator(World worldIn) {
		super(worldIn);

		this.noClip = true;

		this.illuminated = false;

		this.setSize(0.5F, 0.5F);

		setRenderDistanceWeight(0.1D);
	}

	public EntitySpectreIlluminator(World worldIn, double x, double y, double z) {
		this(worldIn);

		this.setPosition(x, y, z);
	}

	public void setTarget(BlockPos bPos) {
		ChunkPos cPos = new ChunkPos(bPos);

		targetX = (cPos.getXStart() + cPos.getXEnd()) / 2D;
		targetZ = (cPos.getZStart() + cPos.getZEnd()) / 2D;
	}

	// Mark as collidable so that the player can pick up the illuminator
	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	public EnumActionResult applyPlayerInteraction(@Nonnull EntityPlayer player, @Nonnull Vec3d vec,
			@Nonnull EnumHand hand) {
		if (!player.world.isRemote) {
			// Prevent duplication of items
			if (!isDead) {
				setDead();

				player.world.spawnEntity(new EntityItem(player.world, this.posX, this.posY,
						this.posZ, new ItemStack(ModItems.spectreIlluminator)));
			}
		}
		return EnumActionResult.SUCCESS;
	}

	@Override
	public void setDead() {
		super.setDead();

		if (!world.isRemote) {
			SpectreIlluminationHandler handler = SpectreIlluminationHandler.get(this.world);

			BlockPos myPosition = this.getPosition();

			if (handler.isIlluminated(myPosition))
				handler.toggleChunk(this.world, myPosition);
		}
	}

	@Override
	public void onEntityUpdate() {
		super.onEntityUpdate();

		if (!this.world.isRemote) {
			BlockPos myPosition = this.getPosition();

			if (targetX == -1 || targetZ == -1)
				setTarget(myPosition);

			Chunk thisChunk = world.getChunkFromBlockCoords(myPosition);

			// Stagger update of targetY to prevent lag
			// Using entity ID as offset to distribute updates semi evenly
			if ((this.world.getTotalWorldTime() + this.getEntityId()) % 100 == 0) {
				// Get highest block in chunk
				targetY = 0;
				for (int y : thisChunk.getHeightMap()) {
					if (y > targetY)
						targetY = y;
				}
				targetY += 3;
			}

			// Calculate distances
			double distX = targetX - this.posX;
			double distY = targetY - this.posY;
			double distZ = targetZ - this.posZ;

			double threshold = 0.01;
			double speed = 0.08;

			if (Math.abs(distX) > threshold) {
				this.motionX = Math.max(-speed, Math.min(speed, distX * 0.1));
			} else {
				this.motionX = 0;
				this.posX = targetX;
			}

			if (Math.abs(distY) > threshold) {
				this.motionY = Math.max(-speed, Math.min(speed, distY * 0.1));
			} else {
				this.motionY = 0;
				this.posY = targetY;
			}

			if (Math.abs(distZ) > threshold) {
				this.motionZ = Math.max(-speed, Math.min(speed, distZ * 0.1));
			} else {
				this.motionZ = 0;
				this.posZ = targetZ;
			}

			// Check if in position and illuminate
			if (Math.abs(distX) < threshold && Math.abs(distY) < threshold
					&& Math.abs(distZ) < threshold) {
				if (!illuminated
						&& !SpectreIlluminationHandler.get(this.world).isIlluminated(myPosition)) {
					SpectreIlluminationHandler.get(this.world).toggleChunk(this.world, myPosition);
					this.illuminated = true;
				}
			}
		}

		this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
	}

	@Override
	protected void entityInit() {}

	@Override
	protected void readEntityFromNBT(@Nonnull NBTTagCompound compound) {
		this.illuminated = compound.getBoolean("illuminated");
	}

	@Override
	protected void writeEntityToNBT(@Nonnull NBTTagCompound compound) {
		compound.setBoolean("illuminated", illuminated);
	}
}
