package com.enderio.core.common.vecmath;

public class Vec2f {

  public float x;
  public float y;

  public Vec2f() {
    x = 0;
    y = 0;
  }

  public Vec2f(double x, double y) {
    this.x = (float) x;
    this.y = (float) y;
  }

  public Vec2f(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public Vec2f(Vec2d other) {
    this(other.x, other.y);
  }

  public Vec2f(Vec2f other) {
    this(other.x, other.y);
  }

  public void set(double x, double y) {
    this.x = (float) x;
    this.y = (float) y;
  }

  public void set(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public void set(Vec2f vec) {
    x = vec.x;
    y = vec.y;
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

  public double distanceSquared(Vec2f v) {
    double dx, dy;
    dx = x - v.x;
    dy = y - v.y;
    return (dx * dx + dy * dy);
  }

  public double distance(Vec2f v) {
    return Math.sqrt(distanceSquared(v));
  }

  @Override
  public String toString() {
    return "Vector2f(" + x + ", " + y + ")";
  }

}
