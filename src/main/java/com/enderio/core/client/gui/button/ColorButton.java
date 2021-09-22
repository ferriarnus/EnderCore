package com.enderio.core.client.gui.button;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.item.DyeColor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.client.render.ColorUtil;
import com.enderio.core.common.vecmath.Vec3f;
import net.minecraft.util.math.MathHelper;

public class ColorButton extends IconButton {

  private int colorIndex = 0;

  private @Nonnull ITextComponent tooltipPrefix = StringTextComponent.EMPTY;

  public ColorButton(@Nonnull IGuiScreen gui, int x, int y) {
    super(gui, x, y, null);
  }

  public ColorButton(@Nonnull IGuiScreen gui, int x, int y, IPressable pressedAction) {
    super(gui, x, y, null, pressedAction);
  }

  @Override
  public void onGuiInit() {
    super.onGuiInit();
    setColorIndex(colorIndex);
  }

  @Override
  public void onClick(double mouseX, double mouseY) {
    super.onClick(mouseX, mouseY);
    nextColor();
  }

  @Override
  public boolean buttonPressed(double mouseX, double mouseY, int button) {
    if (button == 1) {
      prevColor();
      return true;
    }
    return false;
  }

  public @Nonnull ITextComponent getTooltipPrefix() {
    return tooltipPrefix;
  }

  public void setTooltipPrefix(@Nullable ITextComponent tooltipPrefix) {
    if (tooltipPrefix == null) {
      this.tooltipPrefix = StringTextComponent.EMPTY;
    } else {
      this.tooltipPrefix = tooltipPrefix;
    }
  }

  private void nextColor() {
    colorIndex++;
    if (colorIndex >= DyeColor.values().length) {
      colorIndex = 0;
    }
    setColorIndex(colorIndex);
  }

  private void prevColor() {
    colorIndex--;
    if (colorIndex < 0) {
      colorIndex = DyeColor.values().length - 1;
    }
    setColorIndex(colorIndex);
  }

  public int getColorIndex() {
    return colorIndex;
  }

  public void setColorIndex(int colorIndex) {
    this.colorIndex = MathHelper.clamp(colorIndex, 0, DyeColor.values().length - 1);
    ITextComponent color = new TranslationTextComponent(DyeColor.values()[colorIndex].getTranslationKey());
    if (tooltipPrefix.getString().length() > 0) {
      setTooltip(tooltipPrefix, color);
    } else {
      setTooltip(color);
    }
  }

  @Override
  public void renderWidget(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    super.renderWidget(matrixStack, mouseX, mouseY, partialTicks);
    if (this.isVisible()) {
      RenderSystem.disableTexture();
      DyeColor col = DyeColor.values()[colorIndex];
      Vec3f c = ColorUtil.toFloat(col.getColorValue());

      RenderSystem.color3f(c.x, c.y, c.z);
      blit(matrixStack, x + 2, y + 2, 0, 0, getWidth() - 4, getHeight() -4);
      RenderSystem.enableTexture();
    }
  }
}
