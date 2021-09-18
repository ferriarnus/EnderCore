package com.enderio.core.api.client.render;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;

public interface IWidgetMap {

  int getSize();

  @Nonnull
  ResourceLocation getTexture();

  void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y);

  void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y, boolean doDraw);

  void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y, boolean doDraw, boolean flipY);

  void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y, double zLevel, boolean doDraw);

  void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y, double zLevel, boolean doDraw, boolean flipY);

  void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y, double width, double height, double zLevel, boolean doDraw);

  void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y, double width, double height, double zLevel, boolean doDraw, boolean flipY);

   class WidgetMapImpl implements IWidgetMap {

    private final int size;
    private final @Nonnull ResourceLocation res;

    public WidgetMapImpl(int size, @Nonnull ResourceLocation res) {
      this.size = size;
      this.res = res;
    }

    @Override
    public int getSize() {
      return size;
    }

    @Override
    public @Nonnull ResourceLocation getTexture() {
      return res;
    }

    @Override
    public void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y) {
      render(matrixStack,widget, x, y, false);
    }

    @Override
    public void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y, boolean doDraw) {
      render(matrixStack,widget, x, y, widget.getWidth(), widget.getHeight(), 0, doDraw);
    }

    @Override
    public void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y, boolean doDraw, boolean flipY) {
      render(matrixStack,widget, x, y, widget.getWidth(), widget.getHeight(), 0, doDraw, flipY);
    }

    @Override
    public void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y, double zLevel, boolean doDraw) {
      render(matrixStack,widget, x, y, widget.getWidth(), widget.getHeight(), zLevel, doDraw);
    }

    @Override
    public void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y, double zLevel, boolean doDraw, boolean flipY) {
      render(matrixStack,widget, x, y, widget.getWidth(), widget.getHeight(), zLevel, doDraw, flipY);
    }

    @Override
    public void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y, double width, double height, double zLevel, boolean doDraw) {
      render(matrixStack,widget, x, y, width, height, zLevel, doDraw, false);
    }

    @Override
    public void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y, double width, double height, double zLevel, boolean doDraw, boolean flipY) {
      if (doDraw) {
        Minecraft.getInstance().textureManager.bindTexture(getTexture());
        if (flipY) {
          AbstractGui.blit(matrixStack, (int) x, (int) y, (int)width, (int)height, widget.getX(), (float)(widget.getY()+height), (int) width, (int) -height, getSize(), getSize());
        } else {
          AbstractGui.blit(matrixStack, (int) x, (int) y, (int)zLevel, widget.getX(), widget.getY(), (int) width, (int) height, getSize(), getSize());
        }
        final IWidgetIcon overlay = widget.getOverlay();
        if (overlay != null) {
          overlay.getMap().render(matrixStack, overlay, x, y, width, height, zLevel, false, flipY);
        }
      }
    }
  }
}
