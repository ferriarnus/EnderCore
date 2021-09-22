package com.enderio.core.common.vecmath;

public class Vec4d {

  public double x;
  public double y;
  public double z;
  public double w;

  public Vec4d() {
    x = 0;
    y = 0;
    z = 0;
    w = 0;
  }

  public Vec4d(double x, double y, double z, double w) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.w = w;
  }

  public Vec4d(Vec4d other) {
    this(other.x, other.y, other.z, other.w);
  }

  public void set(Vec4d vec) {
    x = vec.x;
    y = vec.y;
    z = vec.z;
    w = vec.w;
  }

  public void set(double x, double y, double z, double w) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.w = w;
  }

  public void add(Vec4d vec) {
    x += vec.x;
    y += vec.y;
    z += vec.z;
    w += vec.w;
  }

  public void sub(Vec4d vec) {
    x -= vec.x;
    y -= vec.y;
    z -= vec.z;
    w -= vec.w;
  }

  public void negate() {
    x = -x;
    y = -y;
    z = -z;
    w = -w;
  }

  public void scale(double s) {
    x *= s;
    y *= s;
    z *= s;
    w *= s;
  }

  public void normalize() {
    double scale = 1.0 / Math.sqrt(x * x + y * y + z * z + w * w);
    scale(scale);
  }

  public double dot(Vec4d other) {
    return x * other.x + y * other.y + z * other.z + w * other.w;
  }

  public double lengthSquared() {
    return x * x + y * y + z * z + w * w;
  }

  public double length() {
    return Math.sqrt(lengthSquared());
  }

  @Override
  public String toString() {
    return "Vector4d(" + x + ", " + y + ", " + z + ", " + w + ")";
  }
}
