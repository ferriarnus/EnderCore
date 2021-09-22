package com.enderio.core.common.vecmath;

public class Vec4f {
  public float x;
  public float y;
  public float z;
  public float w;

  public Vec4f() {
    x = 0;
    y = 0;
    z = 0;
    w = 0;
  }

  public Vec4f(float x, float y, float z, float w) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.w = w;
  }

  public Vec4f(double x, double y, double z, double w) {
    this.x = (float) x;
    this.y = (float) y;
    this.z = (float) z;
    this.w = (float) w;
  }

  public Vec4f(Vec4f other) {
    this(other.x, other.y, other.z, other.w);
  }

  public void interpolate(Vec4f destination, float factor) {
    x = (1 - factor) * x + factor * destination.x;
    y = (1 - factor) * y + factor * destination.y;
    z = (1 - factor) * z + factor * destination.z;
    w = (1 - factor) * w + factor * destination.w;
  }

  public void set(Vec4f vec) {
    x = vec.x;
    y = vec.y;
    z = vec.z;
    w = vec.w;
  }

  public void set(float x, float y, float z, float w) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.w = w;
  }

  public void add(Vec4f vec) {
    x += vec.x;
    y += vec.y;
    z += vec.z;
    w += vec.w;
  }

  public void sub(Vec4f vec) {
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

  public double dot(Vec4f other) {
    return x * other.x + y * other.y + z * other.z + w * other.w;
  }

  public double lengthSquared() {
    return x * x + y * y + z * z + w * w;
  }

  public double length() {
    return Math.sqrt(lengthSquared());
  }

  public Vec3f toVector3f() {
    return new Vec3f(x, y, z);
  }

  @Override
  public String toString() {
    return "Vector4f(" + x + ", " + y + ", " + z + ", " + w + ")";
  }
}
