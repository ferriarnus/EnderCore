package com.enderio.core.client.render;

import java.awt.Color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.vecmath.Vec3f;
import com.enderio.core.common.vecmath.Vec4f;
import com.mojang.blaze3d.systems.RenderSystem;

public final class ColorUtil {

  public static @Nonnull Vec4f toFloat(@Nonnull Color color) {
    float[] rgba = color.getComponents(null);
    return new Vec4f(rgba[0], rgba[1], rgba[2], rgba[3]);
  }

  public static @Nonnull Vec3f toFloat(int rgb) {
    int r = rgb >> 16 & 255;
    int g = rgb >> 8 & 255;
    int b = rgb & 255;
    return new Vec3f(r / 255F, g / 255F, b / 255F);
  }

  public static @Nonnull Vec4f toFloat4(int rgb) {
    int r = rgb >> 16 & 255;
    int g = rgb >> 8 & 255;
    int b = rgb & 255;
    return new Vec4f(r / 255F, g / 255F, b / 255F, 1);
  }

  public static int getRGB(@Nullable Color color) {
    // Note: Constants in java.awt.Color are not @Nonnull-annotated
    return color == null ? 0 : getRGB(color.getRed(), color.getGreen(), color.getBlue());
  }

  public static int getRGBA(@Nullable Color color) {
    return color == null ? 0 : getRGBA(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
  }

  public static int getARGB(@Nullable Color color) {
    return color == null ? 0 : getRGBA(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
  }

  public static int getRGB(@Nonnull Vec3f rgb) {
    return getRGB(rgb.x, rgb.y, rgb.z);
  }

  public static int getRGB(float r, float g, float b) {
    return getRGB((int) (r * 255), (int) (g * 255), (int) (b * 255));
  }

  public static int getRGBA(@Nonnull Vec4f col) {
    return getRGBA(col.x, col.y, col.z, col.w);
  }

  public static int getRGBA(float r, float g, float b, float a) {
    return getRGBA((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));
  }

  public static int getARGB(float r, float g, float b, float a) {
    return getARGB((int) (a * 255), (int) (r * 255), (int) (g * 255), (int) (b * 255));
  }

  public static int getRGB(int r, int g, int b) {
    return (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
  }

  public static int getARGB(int r, int g, int b, int a) {
    return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
  }

  public static int getRGBA(int r, int g, int b, int a) {
    return (r & 0xFF) << 24 | (g & 0xFF) << 16 | (b & 0xFF) << 8 | (a & 0xFF);
  }

  /**
   * Turns an int into a glColor4f function
   *
   * @author Buildcraft team
   */
  public static void setGLColorFromInt(int color) {
    float red = (color >> 16 & 255) / 255.0F;
    float green = (color >> 8 & 255) / 255.0F;
    float blue = (color & 255) / 255.0F;
    RenderSystem.color4f(red, green, blue, 1.0F);
  }

  public static int toHex(int r, int g, int b) {
    int hex = 0;
    hex = hex | ((r) << 16);
    hex = hex | ((g) << 8);
    hex = hex | ((b));
    return hex;
  }

  private ColorUtil() {
  }

}
