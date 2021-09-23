package com.enderio.core.api.client.render;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;

public interface IWidgetIcon {

  int getX();

  int getY();

  int getWidth();

  int getHeight();

  @Nullable
  IWidgetIcon getOverlay();

  @Nonnull
  IWidgetMap getMap();

  @Nonnull
  default TextureAtlasSprite getAsTextureAtlasSprite() {
    return new TAS(this);
  }

  /**
   * TextureAtlasSprite that only has the data needed by Slot for a background image. Won't work anywhere where's animation data is needed.
   *
   */
  static class TAS extends TextureAtlasSprite {

    protected TAS(IWidgetIcon icon) {
      super(new TextureAtlas(icon.getMap().getTexture()), new Info(icon.getMap().getTexture(), icon.getWidth(), icon.getHeight(), AnimationMetadataSection.EMPTY), icon.getMap().getSize(), icon.getWidth() , icon.getHeight(), icon.getX(), icon.getY(), new NativeImage(icon.getWidth(), icon.getHeight(), false));
    }

  }

}
