package com.enderio.core.common.vecmath;

public class Vec2d {

  public double x;
  public double y;

  public Vec2d() {
    x = 0;
    y = 0;
  }

  public Vec2d(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public Vec2d(Vec2d other) {
    this(other.x, other.y);
  }

  public void set(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public void set(Vec2f vec) {
    x = vec.x;
    y = vec.y;
  }

  public void set(Vec2d vec) {
    x = vec.x;
    y = vec.y;
  }

  public void add(Vec2d vec) {
    x += vec.x;
    y += vec.y;
  }

  public void sub(Vec2d vec) {
    x -= vec.x;
    y -= vec.y;
  }

  public void add(Vec2f vec) {
    x += vec.x;
    y += vec.y;
  }

  public void sub(Vec2f vec) {
    x -= vec.x;
    y -= vec.y;
  }

  public void negate() {
    x = -x;
    y = -y;
  }

  public void scale(double s) {
    x *= s;
    y *= s;
  }

  public void normalize() {
    double scale = 1.0 / Math.sqrt(x * x + y * y);
    scale(scale);
  }

  public double lengthSquared() {
    return x * x + y * y;
  }

  public double length() {
    return Math.sqrt(lengthSquared());
  }

  public double distanceSquared(Vec2d v) {
    double dx, dy;
    dx = x - v.x;
    dy = y - v.y;
    return (dx * dx + dy * dy);
  }

  public double distance(Vec2d v) {
    return Math.sqrt(distanceSquared(v));
  }

  @Override
  public String toString() {
    return "Vector2d(" + x + ", " + y + ")";
  }

}
