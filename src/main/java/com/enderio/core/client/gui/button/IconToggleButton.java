package com.enderio.core.client.gui.button;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public class IconToggleButton extends IIconButton {

  private boolean selected = false;

  public IconToggleButton(int x, int y, @Nullable TextureAtlasSprite icon, @Nullable ResourceLocation texture) {
    super(x, y, icon, texture);
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  @Override
  public boolean isHovered() {
    if (selected)
      return false;
    return super.isHovered();
  }
}
