package com.enderio.core.api.client.gui;

import java.io.IOException;

import javax.annotation.Nonnull;

import com.enderio.core.client.gui.GhostSlotHandler;
import com.enderio.core.client.gui.widget.TooltipWidget;
import net.minecraft.client.gui.components.Button;


public interface IGuiScreen {

  void addTooltip(@Nonnull TooltipWidget tooltip);

  boolean removeTooltip(@Nonnull TooltipWidget tooltip);

  void clearTooltips();

  int getGuiRootLeft();

  int getGuiRootTop();

  int getGuiXSize();

  int getGuiYSize();

  @Nonnull
  <T extends Button> T addGuiButton(@Nonnull T button);

  void removeButton(@Nonnull Button button);

  int getOverlayOffsetXLeft();

  int getOverlayOffsetXRight();

  void doActionPerformed(@Nonnull Button but) throws IOException;

  @Nonnull
  GhostSlotHandler getGhostSlotHandler();

}
