package lumien.randomthings.client.gui.elements;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import lumien.randomthings.tileentity.TileEntityEntityDetector.POWER_MODE;

public class GuiPowerModeButton extends GuiCustomButton {
    POWER_MODE powerModeValue;
    String[] tooltips;

    public GuiPowerModeButton(GuiScreen parent, int buttonId, POWER_MODE value, int x, int y, int widthIn, int heightIn,
            String buttonText, ResourceLocation buttonTextures, int uX, int uY, int textureWidth, int textureHeight) {
        super(parent, buttonId, false, x, y, widthIn, heightIn, buttonText, buttonTextures, uX, uY, textureWidth,
                textureHeight);
        this.powerModeValue = value;
        this.tooltips = new String[POWER_MODE.values().length];
    }

    public GuiPowerModeButton(GuiScreen parent, int buttonId, POWER_MODE value, int x, int y, int widthIn, int heightIn,
            String buttonText, ResourceLocation buttonTextures, int uX, int uY) {
        this(parent, buttonId, value, x, y, widthIn, heightIn, buttonText, buttonTextures, uX, uY, widthIn, heightIn);
    }

    @Override
    protected int getTextureOffset() {
        return powerModeValue != null ? powerModeValue.ordinal() * 20 : 0;
    }

    @Override
    protected String getTooltip() {
        if (tooltips == null || powerModeValue == null || powerModeValue.ordinal() >= tooltips.length) {
            return null;
        }
        return tooltips[powerModeValue.ordinal()];
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            mc.getTextureManager().bindTexture(buttonTextures);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width
                    && mouseY < this.y + this.height;
            int k = this.getHoverState(this.hovered);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.blendFunc(770, 771);

            this.drawTexturedModalRect(this.x, this.y, uX + getTextureOffset(), uY + (k - 1) * 20, this.width,
                    this.height);
            this.mouseDragged(mc, mouseX, mouseY);
        }
    }

    public GuiPowerModeButton setToolTips(String weakTooltip, String strongTooltip, String proportionalTooltip) {
        tooltips[POWER_MODE.WEAK.ordinal()] = weakTooltip;
        tooltips[POWER_MODE.STRONG.ordinal()] = strongTooltip;
        tooltips[POWER_MODE.PROPORTIONAL.ordinal()] = proportionalTooltip;

        return this;
    }

    public POWER_MODE getPowerMode() {
        return powerModeValue;
    }

    public void setValue(POWER_MODE value) {
        this.powerModeValue = value;
    }
}
