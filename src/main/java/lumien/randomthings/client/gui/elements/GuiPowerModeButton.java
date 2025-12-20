package lumien.randomthings.client.gui.elements;

import java.util.Arrays;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;

import lumien.randomthings.tileentity.TileEntityEntityDetector.POWER_MODE;

public class GuiPowerModeButton extends GuiButton {
    GuiScreen parent;
    ResourceLocation buttonTextures;
    int uX;
    int uY;
    POWER_MODE value;

    String[] tooltips;

    int textureWidth;
    int textureHeight;

    public GuiPowerModeButton(GuiScreen parent, int buttonId, POWER_MODE value, int x, int y, int widthIn, int heightIn,
            String buttonText, ResourceLocation buttonTextures, int uX, int uY, int textureWidth, int textureHeight) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);

        this.parent = parent;
        this.buttonTextures = buttonTextures;
        this.uX = uX;
        this.uY = uY;
        this.value = value;

        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;

        this.tooltips = new String[POWER_MODE.values().length];
    }

    public GuiPowerModeButton(GuiScreen parent, int buttonId, POWER_MODE value, int x, int y, int widthIn, int heightIn,
            String buttonText, ResourceLocation buttonTextures, int uX, int uY) {
        this(parent, buttonId, value, x, y, widthIn, heightIn, buttonText, buttonTextures, uX, uY, widthIn, heightIn);
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

            // Use ordinal to determine texture offset (each mode is 20 pixels wide)
            int textureOffset = value.ordinal() * 20;
            // Use drawTexturedModalRect for fixed-size buttons to avoid border clipping
            this.drawTexturedModalRect(this.x, this.y, uX + textureOffset, uY + (k - 1) * 20, this.width, this.height);
            this.mouseDragged(mc, mouseX, mouseY);
        }
    }

    @Override
    public void drawButtonForegroundLayer(int mouseX, int mouseY) {
        if (this.tooltips != null && value != null && value.ordinal() < tooltips.length) {
            String toolTip = this.tooltips[value.ordinal()];

            if (toolTip != null) {
                toolTip = I18n.format(toolTip);
                GuiUtils.drawHoveringText(Arrays.<String>asList(new String[] { toolTip }), mouseX, mouseY,
                        parent.mc.displayWidth, parent.mc.displayHeight, -1, parent.mc.fontRenderer);
            }
        }
    }

    public GuiPowerModeButton setToolTips(String weakTooltip, String strongTooltip, String proportionalTooltip) {
        tooltips[POWER_MODE.WEAK.ordinal()] = weakTooltip;
        tooltips[POWER_MODE.STRONG.ordinal()] = strongTooltip;
        tooltips[POWER_MODE.PROPORTIONAL.ordinal()] = proportionalTooltip;

        return this;
    }

    public POWER_MODE getValue() {
        return value;
    }

    public void setValue(POWER_MODE value) {
        this.value = value;
    }
}
