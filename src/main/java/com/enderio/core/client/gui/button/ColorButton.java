package com.enderio.core.client.gui.button;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.client.render.ColorUtil;
import com.enderio.core.common.vecmath.Vec3f;
import net.minecraft.util.Mth;

import net.minecraft.client.gui.components.Button.OnPress;

public class ColorButton extends IconButton {

  private int colorIndex = 0;

  private @Nonnull Component tooltipPrefix = TextComponent.EMPTY;

  public ColorButton(@Nonnull IGuiScreen gui, int x, int y) {
    super(gui, x, y, null);
  }

  public ColorButton(@Nonnull IGuiScreen gui, int x, int y, OnPress pressedAction) {
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

  public @Nonnull Component getTooltipPrefix() {
    return tooltipPrefix;
  }

  public void setTooltipPrefix(@Nullable Component tooltipPrefix) {
    if (tooltipPrefix == null) {
      this.tooltipPrefix = TextComponent.EMPTY;
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
    this.colorIndex = Mth.clamp(colorIndex, 0, DyeColor.values().length - 1);
    Component color = new TranslatableComponent(DyeColor.values()[colorIndex].getName());
    if (tooltipPrefix.getString().length() > 0) {
      setTooltip(tooltipPrefix, color);
    } else {
      setTooltip(color);
    }
  }

  @Override
  public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
    if (this.isVisible()) {
      RenderSystem.disableTexture();
      DyeColor col = DyeColor.values()[colorIndex];
      Vec3f c = ColorUtil.toFloat(col.getFireworkColor());

      RenderSystem.setShaderColor(c.x, c.y, c.z, 1);
      blit(matrixStack, x + 2, y + 2, 0, 0, getWidth() - 4, getHeight() -4);
      RenderSystem.enableTexture();
    }
  }
}
