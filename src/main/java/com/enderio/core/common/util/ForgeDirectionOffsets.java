package com.enderio.core.common.util;

import com.enderio.core.common.vecmath.Vec3d;

import net.minecraft.core.Direction;

public final class ForgeDirectionOffsets {

  public static final Vec3d[] OFFSETS = new Vec3d[Direction.values().length];

  static {
    for (Direction dir : Direction.values()) {
      OFFSETS[dir.ordinal()] = new Vec3d(dir.getStepX(), dir.getStepY(), dir.getStepZ());
    }
  }

  public static Vec3d forDir(Direction dir) {
    return OFFSETS[dir.ordinal()];
  }

  public static Vec3d forDirCopy(Direction dir) {
    return new Vec3d(OFFSETS[dir.ordinal()]);
  }

  public static Vec3d offsetScaled(Direction dir, double scale) {
    Vec3d res = forDirCopy(dir);
    res.scale(scale);
    return res;
  }

  public static Vec3d absolueOffset(Direction dir) {
    Vec3d res = forDirCopy(dir);
    res.x = Math.abs(res.x);
    res.y = Math.abs(res.y);
    res.z = Math.abs(res.z);
    return res;
  }

  public static Direction closest(float x, float y, float z) {
    float ax = Math.abs(x);
    float ay = Math.abs(y);
    float az = Math.abs(z);

    if (ax >= ay && ax >= az) {
      return x > 0 ? Direction.EAST : Direction.WEST;
    }
    if (ay >= ax && ay >= az) {
      return y > 0 ? Direction.UP : Direction.DOWN;
    }
    return z > 0 ? Direction.SOUTH : Direction.NORTH;
  }

  private ForgeDirectionOffsets() {
  }

  public static boolean isPositiveOffset(Direction dir) {
    return dir == Direction.SOUTH || dir == Direction.EAST || dir == Direction.UP;
  }

}
