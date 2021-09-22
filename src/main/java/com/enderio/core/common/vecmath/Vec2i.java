package com.enderio.core.common.vecmath;

public class Vec2i {

  public int x;
  public int y;

  public Vec2i() {
    x = 0;
    y = 0;
  }

  public Vec2i(double x, double y) {
    this.x = (int) x;
    this.y = (int) y;
  }

  public Vec2i(float x, float y) {
    this.x = (int)x;
    this.y = (int)y;
  }
  
  public Vec2i(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public Vec2i(Vec2d other) {
    this(other.x, other.y);
  }

  public Vec2i(Vec2f other) {
    this(other.x, other.y);
  }

  public void set(double x, double y) {
    this.x = (int) x;
    this.y = (int) y;
  }

  public void set(float x, float y) {
    this.x = (int)x;
    this.y = (int)y;
  }

  public void set(Vec2i vec) {
    x = vec.x;
    y = vec.y;
  }

  public void add(Vec2i vec) {
    x += vec.x;
    y += vec.y;
  }

  public void sub(Vec2i vec) {
    x -= vec.x;
    y -= vec.y;
  }

  public void negate() {
    x = -x;
    y = -y;
  }

  public void scale(int s) {
    x *= s;
    y *= s;
  } 

  public double lengthSquared() {
    return x * x + y * y;
  }

  public double length() {
    return Math.sqrt(lengthSquared());
  }

  public double distanceSquared(Vec2i v) {
    double dx, dy;
    dx = x - v.x;
    dy = y - v.y;
    return (dx * dx + dy * dy);
  }

  public double distance(Vec2i v) {
    return Math.sqrt(distanceSquared(v));
  }

  @Override
  public String toString() {
    return "Vector2i(" + x + ", " + y + ")";
  }

}
