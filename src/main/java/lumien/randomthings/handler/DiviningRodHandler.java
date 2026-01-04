package lumien.randomthings.handler;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import lumien.randomthings.config.DiviningRods;
import lumien.randomthings.item.diviningrod.ItemDiviningRod;
import lumien.randomthings.item.diviningrod.RodType;
import lumien.randomthings.util.client.RenderUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DiviningRodHandler {
	public static DiviningRodHandler INSTANCE;

	LinkedHashSet<BlockPos> positionsToCheck;

	List<Indicator> indicators;

	public DiviningRodHandler() {
		positionsToCheck = new LinkedHashSet<BlockPos>();
		indicators = new ArrayList<Indicator>();
	}

	public boolean shouldGlow(RodType rodType) {
		return !indicators.isEmpty() && (indicators.stream().anyMatch((i) -> i.type == rodType));
	}

	int modX, modY, modZ;

	public void render() {
		float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();

		EntityPlayer player = Minecraft.getMinecraft().player;

		double playerX = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
		double playerY = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
		double playerZ = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;

		GlStateManager.disableDepth();
		RenderUtils.enableDefaultBlending();

		GlStateManager.translate(-playerX, -playerY, -playerZ);
		for (Indicator indicator : indicators) {
			float size = (1 - (indicator.duration / 160F)) * 0.2F + 0.1F;
			Color c = indicator.color;
			RenderUtils.drawCube((float) (indicator.target.getX() + 0.5 - size / 2),
					(float) (indicator.target.getY() + 0.5 - size / 2),
					(float) (indicator.target.getZ() + 0.5 - size / 2), size, c.getRed(), c.getGreen(), c.getBlue(),
					c.getAlpha());
		}
		GlStateManager.translate(playerX, playerY, playerZ);

		GlStateManager.enableDepth();
	}

	public void tick() {
		Iterator<Indicator> indicatorIterator = indicators.iterator();

		while (indicatorIterator.hasNext()) {
			Indicator i = indicatorIterator.next();

			i.duration--;

			if (i.duration == 0) {
				indicatorIterator.remove();
			}
		}

		EntityPlayer player = Minecraft.getMinecraft().player;

		if (player != null) {
			World world = player.world;

			if (world != null) {
				ItemStack main = player.getHeldItemMainhand();
				ItemStack off = player.getHeldItemOffhand();

				ItemStack rod = ItemStack.EMPTY;

				if (!main.isEmpty() && main.getItem() instanceof ItemDiviningRod) {
					rod = main;
				} else if (!off.isEmpty() && off.getItem() instanceof ItemDiviningRod) {
					rod = off;
				}

				if (!rod.isEmpty()) {
					BlockPos playerPos = player.getPosition();
					RodType type = ItemDiviningRod.getRodType(rod);
					if (type == null)
						return;

					int range = DiviningRods.RANGE;
					int maxCoord = range + 1; // +1 because it is centered on the player
					int totalBlocks = (range * 2 + 1) * (range * 2 + 1) * (range * 2 + 1);
					// Check all blocks over 20 ticks (roughly 1 second)
					int blocksPerTick = Math.max(1, totalBlocks / 20);

					// Initialize modX, modY, modZ to start at -range if they're out of bounds
					if (modX < -range || modX > range || modY < -range || modY > range
							|| modZ < -range || modZ > range) {
						modX = -range - 1; // Start one before so first increment makes it -range
						modY = -range;
						modZ = -range;
					}

					for (int i = 0; i < blocksPerTick; i++) {
						modX++;

						if (modX == maxCoord) {
							modX = -range;
							modZ++;

							if (modZ == maxCoord) {
								modZ = -range;
								modY++;

								if (modY == maxCoord) {
									modY = -range;
								}
							}
						}

						BlockPos target = playerPos.add(modX, modY, modZ);
						IBlockState blockState = world.getBlockState(target);

						if (world.isBlockLoaded(target)) {
							if (type.matches(world, target, blockState)) {
								Indicator indicator = new Indicator(target, 160,
										type.getIndicatorColor(world, target, blockState), type);

								indicators.add(indicator);
							}
						}
					}
				}
			}
		}
	}

	private static class Indicator {
		BlockPos target;
		int duration;
		Color color;

		RodType type;

		public Indicator(BlockPos target, int duration, Color color, RodType type) {
			super();
			this.target = target;
			this.duration = duration;
			this.color = color;
			this.type = type;
		}
	}

	public static DiviningRodHandler get() {
		if (INSTANCE == null) {
			INSTANCE = new DiviningRodHandler();
		}
		return INSTANCE;
	}
}
