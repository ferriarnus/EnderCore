package com.enderio.core.client.render;

import javax.annotation.Nonnull;

import com.enderio.core.api.client.render.VertexTransform;
import com.enderio.core.common.vecmath.Quat4d;
import com.enderio.core.common.vecmath.Vec3d;
import com.enderio.core.common.vecmath.Vec3f;
import com.enderio.core.common.vecmath.Vertex;

public class VertexRotation implements VertexTransform {

  private final @Nonnull Vec3d center;
  private @Nonnull Quat4d quat;
  private double angle;
  private final @Nonnull Vec3d axis;

  public VertexRotation(double angle, @Nonnull Vec3d axis, @Nonnull Vec3d center) {
    this.center = new Vec3d(center);
    this.axis = new Vec3d(axis);
    this.angle = angle;
    quat = Quat4d.makeRotate(angle, axis);
  }

  @Override
  public void apply(@Nonnull Vertex vertex) {
    apply(vertex.xyz);
  }

  @Override
  public void apply(@Nonnull Vec3d vec) {
    vec.sub(center);
    quat.rotate(vec);
    vec.add(center);
  }

  public void setAngle(double angle) {
    this.angle = angle;
    quat = Quat4d.makeRotate(angle, axis);
  }

  public double getAngle() {
    return angle;
  }

  public void setAxis(@Nonnull Vec3d axis) {
    this.axis.set(axis);
    quat = Quat4d.makeRotate(angle, axis);
  }

  public void setCenter(@Nonnull Vec3d cen) {
    center.set(cen);
  }

  @Override
  public void applyToNormal(@Nonnull Vec3f vec) {
    quat.rotate(vec);
  }

}
