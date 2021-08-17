package com.enderio.core.client.gui;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.enderio.core.client.gui.widget.TooltipWidget;
import com.enderio.core.common.util.NNList;
import com.google.common.collect.Sets;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class TooltipManager {

  public interface TooltipRenderer {

    int getGuiRootLeft();

    int getGuiRootTop();

    int getGuiXSize();

    @Nonnull
    FontRenderer getFontRenderer();

    void drawHoveringTooltipText(MatrixStack matrixStack, @Nonnull List<ITextComponent> tooltips, int x, int y, @Nonnull FontRenderer font);
  }

  private final @Nonnull Set<TooltipWidget> tooltipWidgets = Sets.newHashSet();

  public void addTooltip(@Nonnull TooltipWidget tooltipWidget) {
    tooltipWidgets.add(tooltipWidget);
  }

  public boolean removeTooltip(@Nonnull TooltipWidget tooltipWidget) {
    return tooltipWidgets.remove(tooltipWidget);
  }

  public void clearToolTips() {
    tooltipWidgets.clear();
  }

  protected final void drawTooltips(MatrixStack stack, @Nonnull TooltipRenderer renderer, int mouseX, int mouseY) {
    for (TooltipWidget tooltipWidget : tooltipWidgets) {
      tooltipWidget.onTick(mouseX - renderer.getGuiRootLeft(), mouseY - renderer.getGuiRootTop());
      if (tooltipWidget.shouldDraw()) {
        drawTooltip(stack, tooltipWidget, mouseX, mouseY, renderer);
      }
    }
  }

  protected void drawTooltip(MatrixStack stack, @Nonnull TooltipWidget tooltipWidget, int mouseX, int mouseY, @Nonnull TooltipRenderer renderer) {
    List<ITextComponent> list = tooltipWidget.getTooltipText();
    if (list.isEmpty()) {
      return;
    }

    NNList<ITextComponent> formatted = new NNList<>();
    for (int i = 0; i < list.size(); i++) {
      if (i == 0) {

        formatted.add(applyTextColor(list.get(0), TextFormatting.WHITE));
      } else {
        formatted.add(applyTextColor(list.get(i), TextFormatting.GRAY));
      }
    }
    renderer.drawHoveringTooltipText(stack,formatted, mouseX, mouseY, renderer.getFontRenderer());
  }

  private static ITextComponent applyTextColor(ITextComponent textComponent, TextFormatting textFormatting) {
    textComponent.getStyle().applyFormatting(textFormatting);
    return textComponent;
  }

}
