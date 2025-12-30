package lumien.randomthings.client.render;

import lumien.randomthings.tileentity.TileEntitySpecialChest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSpecialChest extends TileEntitySpecialRenderer<TileEntitySpecialChest>
{
	private static final ResourceLocation textureNatureChest = new ResourceLocation("randomthings:textures/blocks/specialChest/nature.png");
	private static final ResourceLocation textureWaterChest = new ResourceLocation("randomthings:textures/blocks/specialChest/water.png");

	private ModelChest simpleChest = new ModelChest();
	private boolean isChristams;

	public RenderSpecialChest()
	{

	}

    @Override
	public void render(TileEntitySpecialChest te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
        switch (te.getChestType())
        {
        case 0:
            Minecraft.getMinecraft().renderEngine.bindTexture(textureNatureChest);
            break;
        case 1:
            Minecraft.getMinecraft().renderEngine.bindTexture(textureWaterChest);
            break;
        }
        int j;

        if (!te.hasWorld())
        {
            j = 0;
        }
        else
        {
            Block block = te.getBlockType();
            j = te.getBlockMetadata();

            if (block instanceof BlockChest && j == 0)
            {
                ((BlockChest) block).checkForSurroundingChests(te.getWorld(), te.getPos(), te.getWorld().getBlockState(te.getPos()));
                j = te.getBlockMetadata();
            }
        }

        ModelChest modelchest;

        modelchest = this.simpleChest;

        if (destroyStage >= 0)
        {
            this.bindTexture(DESTROY_STAGES[destroyStage]);
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.scale(4.0F, 4.0F, 1.0F);
            GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.matrixMode(5888);
        }

        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();

        if (destroyStage < 0)
        {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        GlStateManager.translate((float) x, (float) y + 1.0F, (float) z + 1.0F);

        GlStateManager.scale(1.0F, -1.0F, -1.0F);
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        short short1 = 0;

        if (j == 2)
        {
            short1 = 180;
        }

        if (j == 3)
        {
            short1 = 0;
        }

        if (j == 4)
        {
            short1 = 90;
        }

        if (j == 5)
        {
            short1 = -90;
        }

        GlStateManager.rotate(short1, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-0.5F, -0.5F, -0.5F);
        float f1 = te.prevLidAngle + (te.lidAngle - te.prevLidAngle) * partialTicks;
        float f2;

        f1 = 1.0F - f1;
        f1 = 1.0F - f1 * f1 * f1;
        modelchest.chestLid.rotateAngleX = -(f1 * (float) Math.PI / 2.0F);
        modelchest.renderAll();
        GlStateManager.disableRescaleNormal();

        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        if (destroyStage >= 0)
        {
            GlStateManager.matrixMode(5890);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
        }
    }
}