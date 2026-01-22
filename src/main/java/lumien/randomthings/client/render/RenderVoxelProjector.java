package lumien.randomthings.client.render;

import lumien.randomthings.handler.magicavoxel.ClientModelLibrary;
import lumien.randomthings.handler.magicavoxel.MagicaVoxelModel;
import lumien.randomthings.tileentity.TileEntityVoxelProjector;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderVoxelProjector extends TileEntitySpecialRenderer<TileEntityVoxelProjector>
{
	public RenderVoxelProjector()
	{

	}

    @Override
	public void render(TileEntityVoxelProjector te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.translate((float) x, (float) y + 2.0F, (float) z);

        MagicaVoxelModel model = ClientModelLibrary.getInstance().getModel(te.getModel());
        if (model != null)
        {
            GlStateManager.translate(0.5, 0, 0.5);
            int scale = te.getScale();
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.rotate(te.getRenderModelRotation(partialTicks), 0, 1, 0);
            GlStateManager.translate(-model.getSizeX() * 1F / 20F / 2f, 0, -model.getSizeZ() * 1F / 20F / 2f);

            model.getRenderModel(te.randomize()).draw(te.ambientLight());

            GlStateManager.scale(-scale, -scale, -scale);
        }

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

	@Override
	public boolean isGlobalRenderer(TileEntityVoxelProjector voxelProjector)
	{
		return true;
	}
}