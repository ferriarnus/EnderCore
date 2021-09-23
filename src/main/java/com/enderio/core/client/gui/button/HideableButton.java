package com.enderio.core.client.gui.button;

import com.enderio.core.api.client.gui.IHideable;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import net.minecraft.client.gui.components.Button.OnPress;

public class HideableButton extends BaseButton implements IHideable {
  public HideableButton(int x, int y, int width, int height, Component buttonText) {
    super(x, y, width, height, buttonText);
  }

  public HideableButton(int x, int y, int width, int height, Component buttonText, OnPress pressedAction) {
    super(x, y, width, height, buttonText, pressedAction);
  }

  public HideableButton(int x, int y, int width, int height, Component buttonText, Button.OnTooltip onTooltip) {
    super(x, y, width, height, buttonText, onTooltip);
  }

  public HideableButton(int x, int y, int width, int height, Component buttonText, OnPress pressedAction, Button.OnTooltip onTooltip) {
    super(x, y, width, height, buttonText, pressedAction, onTooltip);
  }

  @Override
  public void setIsVisible(boolean visible) {
    this.visible = visible;
  }

  @Override
  public boolean isVisible() {
    return visible;
  }
}
