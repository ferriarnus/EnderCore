package com.enderio.core.client.render;

import javax.annotation.Nonnull;

import com.enderio.core.api.client.render.VertexTransform;
import com.enderio.core.common.vecmath.Vec3d;
import com.enderio.core.common.vecmath.Vec3f;
import com.enderio.core.common.vecmath.Vertex;

public class VertexScale implements VertexTransform {
  private final Vec3d center;
  private final double x;
  private final double y;
  private final double z;

  public VertexScale(double x, double y, double z, Vec3d center) {
    this.center = new Vec3d(center);
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public VertexScale(float x, float y, float z, Vec3d center) {
    this.center = new Vec3d(center);
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public VertexScale(Vec3d scale, Vec3d center) {
    this(scale.x, scale.y, scale.z, center);
  }

  public VertexScale(Vec3f scale, Vec3d center) {
    this(scale.x, scale.y, scale.z, center);
  }

  @Override
  public void apply(@Nonnull Vertex vertex) {
    apply(vertex.xyz);
  }

  @Override
  public void apply(@Nonnull Vec3d vec) {
    vec.sub(center);
    vec.x *= x;
    vec.y *= y;
    vec.z *= z;
    vec.add(center);
  }

  @Override
  public void applyToNormal(@Nonnull Vec3f vec) {

  }

}