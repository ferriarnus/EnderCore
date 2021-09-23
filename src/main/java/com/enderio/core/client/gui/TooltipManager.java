package com.enderio.core.client.gui;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.enderio.core.client.gui.widget.TooltipWidget;
import com.enderio.core.common.util.NNList;
import com.google.common.collect.Sets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class TooltipManager {

  public interface TooltipRenderer {

    int getGuiRootLeft();

    int getGuiRootTop();

    int getGuiXSize();

    @Nonnull
    Font getFontRenderer();

    void drawHoveringTooltipText(PoseStack matrixStack, @Nonnull List<Component> tooltips, int x, int y, @Nonnull Font font);
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

  protected final void drawTooltips(PoseStack stack, @Nonnull TooltipRenderer renderer, int mouseX, int mouseY) {
    for (TooltipWidget tooltipWidget : tooltipWidgets) {
      tooltipWidget.onTick(mouseX - renderer.getGuiRootLeft(), mouseY - renderer.getGuiRootTop());
      if (tooltipWidget.shouldDraw()) {
        drawTooltip(stack, tooltipWidget, mouseX, mouseY, renderer);
      }
    }
  }

  protected void drawTooltip(PoseStack stack, @Nonnull TooltipWidget tooltipWidget, int mouseX, int mouseY, @Nonnull TooltipRenderer renderer) {
    List<Component> list = tooltipWidget.getTooltipText();
    if (list.isEmpty()) {
      return;
    }

    NNList<Component> formatted = new NNList<>();
    for (int i = 0; i < list.size(); i++) {
      if (i == 0) {

        formatted.add(applyTextColor(list.get(0), ChatFormatting.WHITE));
      } else {
        formatted.add(applyTextColor(list.get(i), ChatFormatting.GRAY));
      }
    }
    renderer.drawHoveringTooltipText(stack,formatted, mouseX, mouseY, renderer.getFontRenderer());
  }

  private static Component applyTextColor(Component textComponent, ChatFormatting textFormatting) {
    textComponent.getStyle().applyFormat(textFormatting);
    return textComponent;
  }

}
