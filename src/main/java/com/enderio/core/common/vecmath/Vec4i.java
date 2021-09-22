package com.enderio.core.common.vecmath;

public class Vec4i {

  public int x, y, z, w;

  public Vec4i() {
    x = y = z = w = 0;
  }

  public Vec4i(double x, double y, double z, double w) {
    this.x = (int) x;
    this.y = (int) y;
    this.z = (int) z;
    this.w = (int) w;
  }

  public Vec4i(float x, float y, float z, float w) {
    this.x = (int)x;
    this.y = (int)y;
    this.z = (int) z;
    this.w = (int) w;
  }
  
  public Vec4i(int x, int y, int z, int w) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.w = w;
  }

  public Vec4i(Vec4d other) {
    this(other.x, other.y, other.z, other.w);
  }

  public Vec4i(Vec4f other) {
    this(other.x, other.y, other.z, other.w);
  }

  @Override
  public String toString() {
    return "Vector4i(" + x + ", " + y + ", " + z + ", " + w + ")";
  }

}
