package com.enderio.core.client.gui.button;

import java.awt.Rectangle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.client.gui.widget.TooltipWidget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;

import net.minecraft.client.gui.components.Button.OnPress;

public class TooltipButton extends HideableButton {

  protected int xOrigin;
  protected int yOrigin;
  protected @Nonnull IGuiScreen gui;
  protected @Nullable Component[] tooltipText;
  protected @Nullable TooltipWidget tooltipWidget;

  public TooltipButton(@Nonnull IGuiScreen gui, int x, int y, int widthIn, int heightIn, @Nonnull Component buttonText) {
    super(x, y, widthIn, heightIn, buttonText);
    this.gui = gui;
    this.xOrigin = x;
    this.yOrigin = y;
  }

  public TooltipButton(@Nonnull IGuiScreen gui, int x, int y, int widthIn, int heightIn, @Nonnull Component buttonText, OnPress pressedAction) {
    super(x + gui.getGuiRootLeft(), y + gui.getGuiRootLeft(), widthIn, heightIn, buttonText, pressedAction);
    this.gui = gui;
    this.xOrigin = x;
    this.yOrigin = y;
  }

  public void setTooltip(Component... tooltipText) {
    if (tooltipWidget != null) {
      tooltipWidget.setTooltipText(tooltipText);
    } else {
      tooltipWidget = new TooltipWidget(getBounds(), tooltipText);
      gui.addTooltip(tooltipWidget);
    }
    this.tooltipText = tooltipText;
    updateTooltipBounds();
  }

  public void setTooltip(TooltipWidget newTooltip) {
    boolean addTooltip = true;
    if (tooltipWidget != null) {
      addTooltip = gui.removeTooltip(tooltipWidget);
    }
    tooltipWidget = newTooltip;
    if (addTooltip && tooltipWidget != null) {
      gui.addTooltip(tooltipWidget);
    }
    updateTooltipBounds();
  }

  public final @Nonnull Rectangle getBounds() {
    return new Rectangle(xOrigin, yOrigin, getWidth(), getHeight());
  }

  public void onGuiInit() {
    gui.addGuiButton(this);
    if (tooltipWidget != null) {
      gui.addTooltip(tooltipWidget);
    }
    this.x = xOrigin + gui.getGuiRootLeft();
    this.y = yOrigin + gui.getGuiRootTop();
  }

  public void detach() {
    if (tooltipWidget != null) {
      gui.removeTooltip(tooltipWidget);
    }
    gui.removeButton(this);
  }

  public @Nullable
  TooltipWidget getTooltipWidget() {
    return tooltipWidget;
  }

  public void setSize(int width, int height) {
    this.width = width;
    this.height = height;
    updateTooltipBounds();
  }

  public TooltipButton setPosition(int x, int y) {
    xOrigin = x;
    yOrigin = y;
    updateTooltipBounds();
    return this;
  }

  public void setXOrigin(int xOrigin) {
    this.xOrigin = xOrigin;
  }

  public void setYOrigin(int yOrigin) {
    this.yOrigin = yOrigin;
  }

  private void updateTooltipBounds() {
    if (tooltipWidget != null) {
      tooltipWidget.setBounds(new Rectangle(xOrigin, yOrigin, width, height));
    }
  }

  protected void updateTooltip(int mouseX, int mouseY) {
    if (tooltipWidget != null) {
      tooltipWidget.setIsVisible(visible && active);
    }
  }

  protected final void doRenderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
  }

  /**
   * Renders this button to the screen
   */
  @Override
  public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    updateTooltip(mouseX, mouseY);
    doRenderButton(matrixStack, mouseX, mouseY, partialTicks);
  }
}