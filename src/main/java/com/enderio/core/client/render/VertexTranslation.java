package com.enderio.core.client.render;

import javax.annotation.Nonnull;

import com.enderio.core.api.client.render.VertexTransform;
import com.enderio.core.common.vecmath.Vec3d;
import com.enderio.core.common.vecmath.Vec3f;
import com.enderio.core.common.vecmath.Vertex;

public class VertexTranslation implements VertexTransform {

  private double x;
  private double y;
  private double z;

  public VertexTranslation(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public VertexTranslation(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public VertexTranslation(@Nonnull Vec3d trans) {
    this(trans.x, trans.y, trans.z);
  }

  public VertexTranslation(@Nonnull Vec3f trans) {
    this(trans.x, trans.y, trans.z);
  }

  @Override
  public void apply(@Nonnull Vertex vertex) {
    apply(vertex.xyz);
  }

  @Override
  public void apply(@Nonnull Vec3d vec) {
    vec.x += x;
    vec.y += y;
    vec.z += z;
  }

  public void set(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public void set(@Nonnull Vec3d trans) {
    set(trans.x, trans.y, trans.z);
  }

  @Override
  public void applyToNormal(@Nonnull Vec3f vec) {

  }

}
