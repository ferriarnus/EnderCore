package com.enderio.core.common.vecmath;

public class ViewFrustum {

  public static final int LTN = 0;
  public static final int LTF = 1;
  public static final int LBN = 2;
  public static final int LBF = 3;
  public static final int RTN = 4;
  public static final int RTF = 5;
  public static final int RBN = 6;
  public static final int RBF = 7;

  public static final int VERTEX_COUNT = 8;

  public static final int PLANE_COUNT = 6;

  private static final int TOP = 0;
  private static final int BOTTOM = 1;
  private static final int LEFT = 2;
  private static final int RIGHT = 3;
  private static final int NEAR = 4;
  private static final int FAR = 5;

  private Vec4d vertices[] = new Vec4d[8];
  private Vec4d planes[] = new Vec4d[6];

  private Vec3d eye;

  private Vec3d min;
  private Vec3d max;

  public ViewFrustum() {
    eye = new Vec3d();
    min = new Vec3d();
    max = new Vec3d();
    for (int i = 0; i < VERTEX_COUNT; i++) {
      vertices[i] = new Vec4d();
    }
    for (int i = 0; i < PLANE_COUNT; i++) {
      planes[i] = new Vec4d();
    }
  }

  public ViewFrustum(ViewFrustum other) {
    eye = new Vec3d(other.eye);
    min = new Vec3d(other.min);
    max = new Vec3d(other.max);
    for (int i = 0; i < VERTEX_COUNT; i++) {
      vertices[i] = new Vec4d(other.vertices[i]);
    }
    for (int i = 0; i < PLANE_COUNT; i++) {
      planes[i] = new Vec4d(other.planes[i]);
    }
  }

  public boolean containsPoint(Vec3d point) {
    for (Vec4d plane : planes) {
      if (distanceFromPointToPlane(plane, point) < 0) {
        return false;
      }
    }
    return true;
  }

  private static double distanceFromPointToPlane(Vec4d plane, Vec3d point) {
    Vec4d newPoint = new Vec4d(point.x, point.y, point.z, 1);
    return plane.dot(newPoint);
  }

  public Vec4d getVertex(int index) {
    return vertices[index];
  }

  public Vec3d getEye() {
    return eye;
  }

  public Vec3d getMin() {
    return min;
  }

  public Vec3d getMax() {
    return max;
  }

  public Vec4d getLeftPlane() {
    return getPlane(LEFT);
  }

  public Vec4d getRightPlane() {
    return getPlane(RIGHT);
  }

  public Vec4d getTopPlane() {
    return getPlane(TOP);
  }

  public Vec4d getBottomPlane() {
    return getPlane(BOTTOM);
  }

  public Vec4d getNearPlane() {
    return getPlane(NEAR);
  }

  public Vec4d getFarPlane() {
    return getPlane(FAR);
  }

  /**
   * @param ivm
   *          the inverse view transformation matrix
   * @param ipm
   *          the inverse projection matrix
   */
  public void computeFrustum(Mat4d ivm, Mat4d ipm) {

    Mat4d vpm = new Mat4d();
    vpm.mul(ivm, ipm);

    ivm.getTranslation(eye);

    vertices[LBF].set(-1, -1, 1, 1);
    vertices[LTF].set(-1, 1, 1, 1);
    vertices[RTF].set(1, 1, 1, 1);
    vertices[RBF].set(1, -1, 1, 1);
    vertices[LBN].set(-1, -1, -1, 1);
    vertices[LTN].set(-1, 1, -1, 1);
    vertices[RTN].set(1, 1, -1, 1);
    vertices[RBN].set(1, -1, -1, 1);

    min.set(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
    max.set(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);

    for (int i = 0; i < VERTEX_COUNT; i++) {
      vpm.transform(vertices[i]);
      double w = vertices[i].w;
      // apply perspective
      vertices[i].x /= w;
      vertices[i].y /= w;
      vertices[i].z /= w;

      min.x = Math.min(min.x, vertices[i].x);
      min.y = Math.min(min.y, vertices[i].y);
      min.z = Math.min(min.z, vertices[i].z);

      max.x = Math.max(max.x, vertices[i].x);
      max.y = Math.max(max.y, vertices[i].y);
      max.z = Math.max(max.z, vertices[i].z);
    }

    VecmathUtil.computePlaneEquation(vertices[LBN], vertices[LBF], vertices[LTF], planes[LEFT]);
    VecmathUtil.computePlaneEquation(vertices[LBN], vertices[LBF], vertices[LTF], planes[LEFT]);
    VecmathUtil.computePlaneEquation(vertices[RBN], vertices[RTF], vertices[RBF], planes[RIGHT]);
    VecmathUtil.computePlaneEquation(vertices[LTN], vertices[LTF], vertices[RTF], planes[TOP]);
    VecmathUtil.computePlaneEquation(vertices[LBN], vertices[RBF], vertices[LBF], planes[BOTTOM]);
    VecmathUtil.computePlaneEquation(vertices[LBN], vertices[LTN], vertices[RTN], planes[NEAR]);
    VecmathUtil.computePlaneEquation(vertices[LBF], vertices[RTF], vertices[LTF], planes[FAR]);

  }

  private final Vec4d getPlane(int index) {
    assert ((index >= 0) && (index < PLANE_COUNT)) : "Illegal index : 0 <= index < " + PLANE_COUNT;
    return planes[index];
  }

}
