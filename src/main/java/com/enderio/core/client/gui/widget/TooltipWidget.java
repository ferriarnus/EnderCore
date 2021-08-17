package com.enderio.core.client.gui.widget;

import com.enderio.core.api.client.gui.IHideable;
import net.minecraft.util.text.ITextComponent;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

public class TooltipWidget implements IHideable {

  private static final long DELAY = 0;

  protected @Nonnull Rectangle bounds;

  private long mouseOverStart;

  protected final @Nonnull List<ITextComponent> text;

  private int lastMouseX = -1;

  private int lastMouseY = -1;

  private boolean visible = true;

  public TooltipWidget(@Nonnull Rectangle bounds, ITextComponent... lines) {
    this.bounds = bounds;
    text = new ArrayList<>();
    if (lines != null) {
      text.addAll(Arrays.asList(lines));
    }
  }

  public TooltipWidget(@Nonnull Rectangle bounds, List<ITextComponent> lines) {
    this(bounds, lines.toArray(new ITextComponent[0]));
  }

  @Override
  public boolean isVisible() {
    return visible;
  }

  @Override
  public void setIsVisible(boolean visible) {
    this.visible = visible;
  }

  public @Nonnull Rectangle getBounds() {
    return bounds;
  }

  public void setBounds(@Nonnull Rectangle bounds) {
    this.bounds = bounds;
  }

  public void onTick(int mouseX, int mouseY) {
    if (lastMouseX != mouseX || lastMouseY != mouseY) {
      mouseOverStart = 0;
    }

    if (isVisible() && getBounds().contains(mouseX, mouseY)) {

      if (mouseOverStart == 0) {
        mouseOverStart = System.currentTimeMillis();
      }
    } else {
      mouseOverStart = 0;
    }

    lastMouseX = mouseX;
    lastMouseY = mouseY;
  }

  public boolean shouldDraw() {
    if (!isVisible()) {
      return false;
    }
    updateText();
    if (mouseOverStart == 0) {
      return false;
    }
    return System.currentTimeMillis() - mouseOverStart >= DELAY;
  }

  protected void updateText() {
  }

  public int getLastMouseX() {
    return lastMouseX;
  }

  public void setLastMouseX(int lastMouseX) {
    this.lastMouseX = lastMouseX;
  }

  public int getLastMouseY() {
    return lastMouseY;
  }

  public void setLastMouseY(int lastMouseY) {
    this.lastMouseY = lastMouseY;
  }

  public void setTooltipText(ITextComponent... txt) {
    text.clear();
    if (txt != null) {
      text.addAll(Arrays.asList(txt));
    }
  }

  public @Nonnull List<ITextComponent> getTooltipText() {
    return text;
  }
}
