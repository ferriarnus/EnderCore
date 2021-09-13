package com.enderio.core.client.gui.button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.api.client.render.IWidgetIcon;
import com.enderio.core.client.gui.widget.TooltipWidget;
import com.enderio.core.client.render.EnderWidget;
import net.minecraft.util.text.ITextComponent;

public class ToggleButton extends IconButton {

  private boolean selected;
  private final @Nonnull IWidgetIcon unselectedIcon;
  private final @Nonnull IWidgetIcon selectedIcon;

  private TooltipWidget selectedTooltip, unselectedTooltip;
  private boolean paintSelectionBorder;

  public ToggleButton(@Nonnull IGuiScreen gui, int x, int y, @Nonnull IWidgetIcon unselectedIcon, @Nonnull IWidgetIcon selectedIcon) {
    super(gui, x, y, unselectedIcon);
    this.unselectedIcon = unselectedIcon;
    this.selectedIcon = selectedIcon;
    selected = false;
    paintSelectionBorder = true;
  }

  public ToggleButton(@Nonnull IGuiScreen gui, int x, int y, @Nonnull IWidgetIcon unselectedIcon, @Nonnull IWidgetIcon selectedIcon, IPressable pressedAction) {
    super(gui, x, y, unselectedIcon, pressedAction);
    this.unselectedIcon = unselectedIcon;
    this.selectedIcon = selectedIcon;
    selected = false;
    paintSelectionBorder = true;
  }

  public boolean isSelected() {
    return selected;
  }

  public ToggleButton setSelected(boolean selected) {
    this.selected = selected;
    icon = selected ? selectedIcon : unselectedIcon;
    if (selected && selectedTooltip != null) {
      setTooltip(selectedTooltip);
    } else if (!selected && unselectedTooltip != null) {
      setTooltip(unselectedTooltip);
    }
    return this;
  }

  @Override
  protected @Nonnull IWidgetIcon getIconForState() {
    if (!selected || !paintSelectionBorder) {
      return super.getIconForState();
    }
    if (!isActive()) {
      return EnderWidget.BUTTON_DISABLED;
    }
    if (isHovered()) {
      return EnderWidget.BUTTON_DOWN_HIGHLIGHT;
    }
    return EnderWidget.BUTTON_DOWN;
  }

  @Override
  public void onClick(double mouseX, double mouseY) {
    super.onClick(mouseX, mouseY);
    toggleSelected();
  }

  protected boolean toggleSelected() {
    setSelected(!selected);
    return true;
  }

  public void setSelectedTooltip(ITextComponent... tooltip) {
    selectedTooltip = new TooltipWidget(getBounds(), makeCombinedTooltipList(tooltip));
    setSelected(selected);
  }

  private @Nonnull List<ITextComponent> makeCombinedTooltipList(ITextComponent... tooltip) {
    final @Nonnull List<ITextComponent> list = new ArrayList<>();
    if (tooltipText != null) {
      Collections.addAll(list, tooltipText);
    }
    Collections.addAll(list, tooltip);
    return list;
  }

  public void setUnselectedTooltip(ITextComponent... tooltip) {
    unselectedTooltip = new TooltipWidget(getBounds(), makeCombinedTooltipList(tooltip));
    setSelected(selected);
  }

  public void setPaintSelectedBorder(boolean b) {
    paintSelectionBorder = b;
  }
}
