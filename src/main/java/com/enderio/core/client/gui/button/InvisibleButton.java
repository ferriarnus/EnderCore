package com.enderio.core.client.gui.button;

import javax.annotation.Nonnull;

import com.enderio.core.api.client.gui.IGuiScreen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.TextComponent;

import net.minecraft.client.gui.components.Button.OnPress;

public class InvisibleButton extends TooltipButton {
  private static final int DEFAULT_WIDTH = 8;
  private static final int DEFAULT_HEIGHT = 6;

  public InvisibleButton(@Nonnull IGuiScreen gui, int x, int y, OnPress pressedAction) {
    super(gui, x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, new TextComponent(""), pressedAction);
  }

  public InvisibleButton(@Nonnull IGuiScreen gui, int x, int y, int width, int height, OnPress pressedAction) {
    super(gui, x, y, width, height, new TextComponent(""), pressedAction);
  }

  @Override
  public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    updateTooltip(mouseX, mouseY);
  }
}
