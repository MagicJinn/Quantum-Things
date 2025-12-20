package lumien.randomthings.client.render;

import java.awt.Color;
import java.util.function.Function;

import javax.annotation.Nonnull;

import lumien.randomthings.client.render.magiccircles.ColorFunctions;
import lumien.randomthings.client.render.magiccircles.IColorFunction;
import lumien.randomthings.entitys.EntitySpectreIlluminator;
import lumien.randomthings.handler.RTEventHandler;
import lumien.randomthings.util.client.MKRRenderUtil;
import lumien.randomthings.util.client.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSpectreIlluminator extends Render<EntitySpectreIlluminator>
{
	// Pre-calculated constants
	private static final float PROGRESS_MULTIPLIER = 2.0F;
	private static final float RADIUS_BASE = 0.15F;
	private static final float RADIUS_INCREMENT = 0.06F;
	private static final float OSC_BASE = 0.005F;
	private static final float OSC_INCREMENT = 0.004F;
	private static final float OSC_SPEED = 0.012F;
	private static final float ROTATION_BASE_X = 72.0F;
	private static final float ROTATION_BASE_Z_OFFSET = 45.0F;
	private static final float ROTATION_MODULO = 360.0F;
	private static final float SPEED_DIVISOR_BASE = 2.0F;
	private static final int SPEED_DIVISOR_MODULO = 3;
	private static final int MAX_LOOP_COUNT = 5;
	private static final int MIN_LOOP_COUNT = 1;
	private static final double Y_OFFSET = 0.25;
	private static final double INNER_RADIUS = 0.04;
	private static final int INNER_TRI_COUNT = 33;
	private static final int OUTER_TRI_COUNT = 30;
	private static final int FLICKER_MULTIPLIER = 200;
	private static final int FLICKER_SLOW = 40;

	// Pre-calculated color constants
	private static final float HSB_HUE = 0.5F;
	private static final float HSB_SATURATION = 1.0F;
	private static final float HSB_BRIGHTNESS_MIN = 0.9F;
	private static final float HSB_BRIGHTNESS_RANGE = 0.1F;
	private static final float COLOR_PHASE_MULTIPLIER = (float) (Math.PI * 4.0 / 50.0); // ~0.251327
	private static final float COLOR_PROGRESS_DIVISOR = 10.0F;
	private static final int COLOR_ALPHA = 255;

	// Cached outer function (doesn't depend on frame state)
	private static final IColorFunction OUTER_FUNCTION_BASE = ColorFunctions.alternate(
			new Color(100, 100, 100, 0),
			new Color(0, 150, 200, 100)).next(
					ColorFunctions.limit(
							ColorFunctions.constant(new Color(0, 0, 0, 0)),
							(i) -> (i + 2) % 3 == 0));

	// Cached constant function for inner count
	private static final Function<Integer, Integer> INNER_COUNT_FUNCTION = (i) -> 3;

	public RenderSpectreIlluminator(RenderManager renderManager)
	{
		super(renderManager);
	}

	@Override
	@SuppressWarnings("null")
	public void doRender(EntitySpectreIlluminator entity, double x, double y, double z, float entityYaw, float partialTicks)
	{
		GlStateManager.disableTexture2D();

		RenderUtils.enableDefaultBlending();

		GlStateManager.color(1, 1, 1, 1);
		RenderHelper.disableStandardItemLighting();

		Minecraft mc = Minecraft.getMinecraft();
		EntityRenderer entityRenderer = mc.entityRenderer;
		entityRenderer.disableLightmap();

		GlStateManager.disableCull();

		GlStateManager.pushMatrix();

		// Shift rendering up by half the entity height to align visual with hitbox
		// Only apply offset when rendering as entity (not as item in hand)
		if (entity != null)
			y += Y_OFFSET;

		GlStateManager.translate(x, y, z);

		// Pre-calculate
		float progress = PROGRESS_MULTIPLIER * (RTEventHandler.clientAnimationCounter + partialTicks);
		float progressDiv10 = progress / COLOR_PROGRESS_DIVISOR;
		float progressOsc = progress * OSC_SPEED;

		// Calculate loop count based on distance to player
		int loopCount = MAX_LOOP_COUNT; // Default
		if (entity != null) {
			EntityPlayer player = mc.player;
			if (player != null) {
				double dx = entity.posX - player.posX;
				double dy = entity.posY - player.posY;
				double dz = entity.posZ - player.posZ;
				// Compare squared distance instead of calculating sqrt
				double distanceSquared = dx * dx + dy * dy + dz * dz;

				// Reduce by 1 for every 16 blocks, minimum of 1
				// Using squared distance thresholds: 16^2=256, 32^2=1024, 48^2=2304, 64^2=4096
				if (distanceSquared > 4096.0) { // > 64 blocks
					loopCount = MIN_LOOP_COUNT;
				} else if (distanceSquared > 2304.0) { // > 48 blocks
					loopCount = 2;
				} else if (distanceSquared > 1024.0) { // > 32 blocks
					loopCount = 3;
				} else if (distanceSquared > 256.0) { // > 16 blocks
					loopCount = 4;
				}
			}
		}

		// Pre-calculate before loop
		float progressY = progress;

		for (int c = 0; c < loopCount; c++)
		{
			// Pre-calculate base rotations (modulo can be optimized with bitwise for
			// power-of-2, but 360 isn't)
			float cTimes72 = c * ROTATION_BASE_X;
			float baseRotX = cTimes72 % ROTATION_MODULO;
			float baseRotZ = (cTimes72 + ROTATION_BASE_Z_OFFSET) % ROTATION_MODULO;

			// Pre-calculate speed divisors (modulo 3 can be optimized)
			int cMod3 = c % SPEED_DIVISOR_MODULO;
			int cPlus2Mod3 = (c + 2) % SPEED_DIVISOR_MODULO;
			float speedDivisorX = cMod3 + SPEED_DIVISOR_BASE;
			float speedDivisorZ = cPlus2Mod3 + SPEED_DIVISOR_BASE;

			float rotX = baseRotX + progress / speedDivisorX;
			float rotZ = baseRotZ + progress / speedDivisorZ;

			// Pre-calculate radius components
			float base = RADIUS_BASE + RADIUS_INCREMENT * c;
			float osc = (OSC_BASE + OSC_INCREMENT * c) * (float) Math.sin(progressOsc + c);
			float radius = base + osc;

			GlStateManager.pushMatrix();
			GlStateManager.rotate(rotX, 1, 0, 0);
			GlStateManager.rotate(rotZ, 0, 0, 1);
			GlStateManager.rotate(progressY, 0, 1, 0);

			// Use cached constant function and pre-calculated values
			MKRRenderUtil.renderCircleDecTriInner(INNER_RADIUS, (i) -> {
				// Pre-calculate phase value
				float phase = COLOR_PHASE_MULTIPLIER * i + progressDiv10;
				float brightness = (float) Math.sin(phase) * HSB_BRIGHTNESS_RANGE + HSB_BRIGHTNESS_MIN;
				Color clr = Color.getHSBColor(HSB_HUE, HSB_SATURATION, brightness);
				// Create color with alpha directly - more efficient than
				// getRed/getGreen/getBlue
				return new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), COLOR_ALPHA);
			}, INNER_TRI_COUNT, INNER_COUNT_FUNCTION);

			int flickerOffset = c * FLICKER_MULTIPLIER;
			MKRRenderUtil.renderCircleDecTriPart3Tri(radius, INNER_RADIUS,
					OUTER_FUNCTION_BASE.next(ColorFunctions.flicker(flickerOffset, FLICKER_SLOW)).tt(progress),
					OUTER_TRI_COUNT);
			GlStateManager.popMatrix();
		}

		GlStateManager.popMatrix();

		GlStateManager.enableCull();

		RenderHelper.enableStandardItemLighting();
		entityRenderer.enableLightmap();

		GlStateManager.color(1, 1, 1, 1);

		GlStateManager.enableTexture2D();
	}

	@Override
	protected ResourceLocation getEntityTexture(@Nonnull EntitySpectreIlluminator entity)
	{
		return null;
	}
}
