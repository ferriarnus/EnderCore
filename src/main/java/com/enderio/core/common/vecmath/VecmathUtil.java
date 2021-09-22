package com.enderio.core.common.vecmath;

import java.awt.Rectangle;

import javax.annotation.Nonnull;

public class VecmathUtil {

  /**
   * Returns the distance from a point to a plane.
   * 
   * @param plane
   *          the plane.
   * @param point
   *          the point.
   * @return the distance between them.
   */
  public static double distanceFromPointToPlane(Vec4d plane, Vec3d point) {
    Vec4d newPoint = new Vec4d(point.x, point.y, point.z, 1);
    return plane.dot(newPoint);
  }

  public static void computePlaneEquation(Vec4d a, Vec4d b, Vec4d c, Vec4d result) {
    computePlaneEquation(new Vec3d(a.x, a.y, a.z), new Vec3d(b.x, b.y, b.z), new Vec3d(c.x, c.y, c.z), result);
  }

  public static @Nonnull Vec3d clamp(Vec3d v, double min, double max) {
    v.x = clamp(v.x, min, max);
    v.y = clamp(v.y, min, max);
    v.z = clamp(v.z, min, max);
    return v;
  }

  public static double clamp(double val, double min, double max) {
    return val < min ? min : (val > max ? max : val);
  }

  public static int clamp(int val, int min, int max) {
    return val < min ? min : (val > max ? max : val);
  }

  /**
   * Compute the plane equation <code>Ax + By + Cz + D = 0</code> for the plane
   * defined by the three points which lie on the plane, a, b, and c, and
   * placing the result into r. The plane equation can be summarised as the
   * normal vector of the plane (A,B,C) and the distance to the plane from the
   * origin (D).
   * 
   * @param a
   *          vector a.
   * @param b
   *          vector b.
   * @param c
   *          vector c.
   * @param result
   *          the result (A,B,C,D) of plane equation.
   */
  public static void computePlaneEquation(Vec3d a, Vec3d b, Vec3d c, Vec4d result) {
    Vec3d i = new Vec3d();
    Vec3d j = new Vec3d();
    Vec3d k = new Vec3d();

    // compute normal vector
    i.x = c.x - a.x;
    i.y = c.y - a.y;
    i.z = c.z - a.z;

    j.x = b.x - a.x;
    j.y = b.y - a.y;
    j.z = b.z - a.z;

    k.cross(j, i);
    k.normalize();

    // plane equation: Ax + By + Cz + D = 0
    result.x = k.x; // A
    result.y = k.y; // B
    result.z = k.z; // C
    result.w = -(result.x * a.x + result.y * a.y + result.z * a.z); // D
  }

  /**
   * Projects the point onto the plane.
   * 
   * @param plane
   *          the plane.
   * @param point
   *          the point.
   */
  public static void projectPointOntoPlane(Vec4d plane, Vec4d point) {
    double distance = plane.dot(point);
    Vec4d newPoint = new Vec4d(point);
    Vec3d planeNormal = new Vec3d(plane.x, plane.y, plane.z);
    planeNormal.normalize();
    planeNormal.scale(distance);
    newPoint.sub(new Vec4d(planeNormal.x, planeNormal.y, planeNormal.z, 0));
    point.set(newPoint);
  }

  /**
   * This method calculates the intersection between a line and a plane.
   * 
   * @param plane
   *          the plane (x,y,z = normal, w = distance from origin)
   * @param pointInLine
   *          a point in the line.
   * @param lineDirection
   *          the direction of the line.
   * @return the intersection or null if there is no intersection or the line is
   *         on the plane.
   */
  public static Vec3d computeIntersectionBetweenPlaneAndLine(Vec4d plane, Vec3d pointInLine, Vec3d lineDirection) {
    // check for no intersection
    Vec3d planeNormal = new Vec3d(plane.x, plane.y, plane.z);
    if (planeNormal.dot(lineDirection) == 0) {
      // line and plane are perpendicular
      return null;
    }
    // check if line is on the plane
    if (planeNormal.dot(pointInLine) + plane.w == 0) {
      return null;
    }

    // we have an intersection
    Vec4d point = new Vec4d(pointInLine.x, pointInLine.y, pointInLine.z, 1);
    Vec4d lineNorm = new Vec4d(lineDirection.x, lineDirection.y, lineDirection.z, 0);
    double t = -(plane.dot(point) / plane.dot(lineNorm));

    Vec3d result = new Vec3d(pointInLine);
    lineDirection.scale(t);
    result.add(lineDirection);
    return result;
  }

  /**
   * This function computes the ray that goes from the eye, through the
   * specified pixel.
   * 
   * @param x
   *          the x pixel location (x = 0 is the left most pixel)
   * @param y
   *          the y pixel location (y = 0 is the bottom most pixel)
   * @param eyeOut
   *          the eyes position.
   * @param normalOut
   *          the normal description the directional component of the ray.
   */
  public static void computeRayForPixel(Rectangle vp, Mat4d ipm, Mat4d ivm, int x, int y, Vec3d eyeOut, Vec3d normalOut) {

    // grab the eye's position
    ivm.getTranslation(eyeOut);

    Mat4d vpm = new Mat4d();
    vpm.mul(ivm, ipm);

    // Calculate the pixel location in screen clip space (width and height from
    // -1 to 1)
    double screenX = (x - vp.getX()) / vp.getWidth();
    double screenY = (y - vp.getY()) / vp.getHeight();
    screenX = (screenX * 2.0) - 1.0;
    screenY = (screenY * 2.0) - 1.0;

    // Now calculate the XYZ location of this point on the near plane
    Vec4d tmp = new Vec4d();
    tmp.x = screenX;
    tmp.y = screenY;
    tmp.z = -1;
    tmp.w = 1.0;
    vpm.transform(tmp);

    double w = tmp.w;
    Vec3d nearXYZ = new Vec3d(tmp.x / w, tmp.y / w, tmp.z / w);

    // and then on the far plane
    tmp.x = screenX;
    tmp.y = screenY;
    tmp.z = 1;
    tmp.w = 1.0;
    vpm.transform(tmp);

    w = tmp.w;
    Vec3d farXYZ = new Vec3d(tmp.x / w, tmp.y / w, tmp.z / w);

    normalOut.set(farXYZ);
    normalOut.sub(nearXYZ);
    normalOut.normalize();

  }

  // /**
  // * Make a rotation Quat which will rotate vec1 to vec2
  // * <p/>
  // * This routine uses only fast geometric transforms, without costly acos/sin
  // computations. It's
  // * exact, fast, and with less degenerate cases than the acos/sin method.
  // * <p/>
  // * For an explanation of the math used, you may see for example:
  // * http://logiciels.cnes.fr/MARMOTTES/marmottes-mathematique.pdf
  // * <p/>
  // * NB: This is the rotation with shortest angle, which is the one equivalent
  // to the acos/sin
  // * transform method. Other rotations exists, for example to additionally
  // keep a local horizontal
  // * attitude.
  // *
  // * @param from
  // * rotate from this vector
  // * @param to
  // * to this one
  // * @return the rotation to apply to from to get to to.
  // */
  //
  // public static Quat4d makeRotate( Vector3d from, Vector3d to) {
  //
  // Quat4d res = new Quat4d();
  //
  // // This routine takes any vector as argument but normalized
  // // vectors are necessary, if only for computing the dot product.
  // // Too bad the API is that generic, it leads to performance loss.
  // // Even in the case the 2 vectors are not normalized but same length,
  // // the sqrt could be shared, but we have no way to know beforehand
  // // at this point, while the caller may know.
  // // So, we have to test... in the hope of saving at least a sqrt
  // Vector3d sourceVector = new Vector3d(from);
  // Vector3d targetVector = new Vector3d(to);
  //
  // double fromLen2 = sourceVector.lengthSquared();
  // double fromLen;
  // // normalize only when necessary, epsilon test
  // if ((fromLen2 < 1.0 - 1e-7) || (fromLen2 > 1.0 + 1e-7)) {
  // fromLen = Math.sqrt(fromLen2);
  // sourceVector.x /= fromLen;
  // sourceVector.y /= fromLen;
  // sourceVector.z /= fromLen;
  //
  // } else {
  // fromLen = 1.0;
  // }
  //
  // double toLen2 = targetVector.lengthSquared();
  // // normalize only when necessary, epsilon test
  // if ((toLen2 < 1.0 - 1e-7) || (toLen2 > 1.0 + 1e-7)) {
  // double toLen;
  // // re-use fromLen for case of mapping 2 vectors of the same length
  // if ((toLen2 > fromLen2 - 1e-7) && (toLen2 < fromLen2 + 1e-7)) {
  // toLen = fromLen;
  // } else {
  // toLen = Math.sqrt(toLen2);
  // }
  //
  // targetVector.x /= toLen;
  // targetVector.y /= toLen;
  // targetVector.z /= toLen;
  // }
  //
  // // Now let's get into the real stuff
  // // Use "dot product plus one" as test as it can be re-used later on
  // double dotProdPlus1 = 1.0 + sourceVector.dot(targetVector);
  //
  // // Check for degenerate case of full u-turn. Use epsilon for detection
  // if (dotProdPlus1 < 1e-7) {
  //
  // // Get an orthogonal vector of the given vector
  // // in a plane with maximum vector coordinates.
  // // Then use it as quaternion axis with pi angle
  // // Trick is to realize one value at least is >0.6 for a normalized vector.
  // if (Math.abs(sourceVector.x) < 0.6) {
  //
  // double norm = Math.sqrt(1.0 - sourceVector.x * sourceVector.x);
  // res.x = 0.0;
  // res.y = sourceVector.z / norm;
  // res.z = -sourceVector.y / norm;
  // res.w = 0.0;
  // } else if (Math.abs(sourceVector.y) < 0.6) {
  // double norm = Math.sqrt(1.0 - sourceVector.y * sourceVector.y);
  // res.x = -sourceVector.z / norm;
  // res.y = 0.0;
  // res.z = sourceVector.x / norm;
  // res.w = 0.0;
  // } else {
  // double norm = Math.sqrt(1.0 - sourceVector.z * sourceVector.z);
  // res.x = sourceVector.y / norm;
  // res.y = -sourceVector.x / norm;
  // res.z = 0.0;
  // res.w = 0.0;
  // }
  // } else {
  // // Find the shortest angle quaternion that transforms normalized vectors
  // // into one other. Formula is still valid when vectors are colinear
  // double s = Math.sqrt(0.5 * dotProdPlus1);
  // double val = (2.0 * s);
  // targetVector.x /= val;
  // targetVector.y /= val;
  // targetVector.z /= val;
  //
  // Vector3d tmp = new Vector3d();
  // tmp.cross(sourceVector, targetVector);
  //
  // res.x = tmp.x;
  // res.y = tmp.y;
  // res.z = tmp.z;
  // res.w = s;
  // }
  //
  // return res;
  // }
  //
  // /**
  // * Makes a rotation of angle (radians) around the axis specified by the
  // x,y,z values.
  // *
  // * @param angle
  // * the angle in radians for the rotation
  // * @param axis
  // * the axis to rotate about.
  // * @return the quaternion defining the rotation.
  // */

  /**
   * Creates a perspective projection matrix.
   * 
   * @param fovDegrees
   *          The field of view angle in degrees.
   * @param near
   *          near plane.
   * @param far
   *          far plane.
   * @param viewportWidth
   *          viewport width.
   * @param viewportHeight
   *          viewport height.
   * @return the matrix.
   */

  public static Mat4d createProjectionMatrixAsPerspective(double fovDegrees, double near, double far, int viewportWidth, int viewportHeight) {

    Mat4d matrix = new Mat4d();
    // for impl details see gluPerspective doco in OpenGL reference manual
    double aspect = (double) viewportWidth / (double) viewportHeight;

    double theta = (Math.toRadians(fovDegrees) / 2d);
    double f = Math.cos(theta) / Math.sin(theta);

    double a = (far + near) / (near - far);
    double b = (2d * far * near) / (near - far);

    matrix.set(new double[] { f / aspect, 0, 0, 0, 0, f, 0, 0, 0, 0, a, b, 0, 0, -1, 0 });

    return matrix;
  }

  /**
   * Creates a projection matrix as per glFrustrum.
   * 
   * @param left
   *          coordinate of left clip plane.
   * @param right
   *          coordinate of right clip plane.
   * @param bottom
   *          coordinate of bottom clip plane.
   * @param top
   *          coordinate of left top plane.
   * @param zNear
   *          distance of the near plane.
   * @param zFar
   *          distance of the near plane.
   * @return the matrix.
   */

  public static Mat4d createProjectionMatrixAsPerspective(double left, double right, double bottom, double top, double zNear, double zFar) {

    double A = (right + left) / (right - left);
    double B = (top + bottom) / (top - bottom);
    double C = (Math.abs(zFar) > Double.MAX_VALUE) ? -1. : -(zFar + zNear) / (zFar - zNear);
    double D = (Math.abs(zFar) > Double.MAX_VALUE) ? -2. * zNear : -2.0 * zFar * zNear / (zFar - zNear);

    Mat4d matrix = new Mat4d();
    matrix.set(new double[] { 2.0 * zNear / (right - left), 0.0, 0.0, 0.0, 0.0, 2.0 * zNear / (top - bottom), 0.0, 0.0, A, B, C, -1.0, 0.0, 0.0, D, 0.0

    });

    matrix.transpose();
    return matrix;
  }

  /**
   * Sets the orthographic projection matrix.
   * 
   * @param left
   *          the left value.
   * @param right
   *          the right value.
   * @param bottom
   *          the bottom value.
   * @param top
   *          the top value.
   * @param near
   *          near plane.
   * @param far
   *          far plane.
   * @return the ortho matrix.
   */

  public static Mat4d createProjectionMatrixAsOrtho(double left, double right, double bottom, double top, double near, double far) {

    Mat4d matrix = new Mat4d();
    // for impl details see glOrtho doco in OpenGL reference manual
    double tx = -((right + left) / (right - left));
    double ty = -((top + bottom) / (top - bottom));
    double tz = -((far + near) / (far - near));

    matrix.set(new double[] { 2d / (right - left), 0, 0, tx, 0, 2d / (top - bottom), 0, ty, 0, 0, -2d / (far - near), tz, 0, 0, 0, 1 });

    return matrix;
  }

  /**
   * Sets the near and far values on an existing perspective projection matrix.
   * 
   * @param projMat
   *          the matrix to be modified.
   * @param near
   *          the new near value.
   * @param far
   *          the new far value.
   */
  public static void setNearFarOnPerspectiveProjectionMatrix(Mat4d projMat, double near, double far) {

    projMat.transpose();

    double transNearPlane = (-near * projMat.getElement(2, 2) + projMat.getElement(3, 2)) / (-near * projMat.getElement(2, 3) + projMat.getElement(3, 3));
    double transFarPlane = (-far * projMat.getElement(2, 2) + projMat.getElement(3, 2)) / (-far * projMat.getElement(2, 3) + projMat.getElement(3, 3));

    double ratio = Math.abs(2.0 / (transNearPlane - transFarPlane));
    double center = -(transNearPlane + transFarPlane) / 2.0;

    Mat4d mat = new Mat4d();
    mat.setIdentity();
    mat.setElement(2, 2, ratio);
    mat.setElement(3, 2, center * ratio);

    projMat.mul(mat);
    projMat.transpose();

  }

  /**
   * Creates a look at matrix.
   * 
   * @param eyePos
   *          the position of the eye.
   * @param lookAtPos
   *          the point to look at.
   * @param upVec
   *          the up vector.
   * @return the look at matrix.
   */

  public static Mat4d createMatrixAsLookAt(Vec3d eyePos, Vec3d lookAtPos, Vec3d upVec) {

    Vec3d eye = new Vec3d(eyePos);
    Vec3d lookAt = new Vec3d(lookAtPos);
    Vec3d up = new Vec3d(upVec);

    Vec3d forwardVec = new Vec3d(lookAt);
    forwardVec.sub(eye);
    forwardVec.normalize();

    Vec3d sideVec = new Vec3d();
    sideVec.cross(forwardVec, up);
    sideVec.normalize();

    Vec3d upVed = new Vec3d();
    upVed.cross(sideVec, forwardVec);
    upVed.normalize();

    Mat4d mat = new Mat4d(sideVec.x, sideVec.y, sideVec.z, 0, upVed.x, upVed.y, upVed.z, 0, -forwardVec.x, -forwardVec.y, -forwardVec.z, 0, 0, 0, 0, 1);

    eye.negate();
    // mat.transform(eye);
    mat.transformNormal(eye);
    mat.setTranslation(eye);

    return mat;
  }

  /**
   * Pre-multiplies the vector by the matrix.
   * 
   * @param v
   *          the vector.
   * @param mat
   *          the matrix.
   * @return the result of the multiplication.
   */

  public static Vec3d preMultiply(Vec3d v, Mat4d mat) {
    Mat4d m = new Mat4d(mat);
    m.transpose();
    double d = 1.0f / (m.getElement(0, 3) * v.x + m.getElement(1, 3) * v.y + m.getElement(2, 3) * v.z + m.getElement(3, 3));
    double x = (m.getElement(0, 0) * v.x + m.getElement(1, 0) * v.y + m.getElement(2, 0) * v.z + m.getElement(3, 0)) * d;
    double y = (m.getElement(0, 1) * v.x + m.getElement(1, 1) * v.y + m.getElement(2, 1) * v.z + m.getElement(3, 1)) * d;
    double z = (m.getElement(0, 2) * v.x + m.getElement(1, 2) * v.y + m.getElement(2, 2) * v.z + m.getElement(3, 2) * d);
    return new Vec3d(x, y, z);
  }

  /**
   * Extracts the s,t,r and q eye planes from the specified matrix.
   * 
   * @param matrix
   *          the matrix to extract the planes from.
   * @return the s,t,r and q planes from the specified matrix.
   */

  public static Vec4d[] getEyePlanesForMatrix(Mat4d matrix) {

    Mat4d copy = new Mat4d(matrix);
    copy.transpose();

    Vec4d[] res = new Vec4d[4];
    // s plane
    res[0] = new Vec4d(copy.getElement(0, 0), copy.getElement(1, 0), copy.getElement(2, 0), copy.getElement(3, 0));
    // t plane
    res[1] = new Vec4d(copy.getElement(0, 1), copy.getElement(1, 1), copy.getElement(2, 1), copy.getElement(3, 1));
    // r plane
    res[2] = new Vec4d(copy.getElement(0, 2), copy.getElement(1, 2), copy.getElement(2, 2), copy.getElement(3, 2));
    // q plane
    res[3] = new Vec4d(copy.getElement(0, 3), copy.getElement(1, 3), copy.getElement(2, 3), copy.getElement(3, 3));

    return res;
  }

  /**
   * Computes the cross product of the two tuples.
   * 
   * @param vec1
   *          the first tuple.
   * @param vec2
   *          the second tuple.
   * @return the cross product of the two tuples.
   */

  public static Vec3d cross(Vec3d vec1, Vec3d vec2) {
    Vec3d res = new Vec3d();
    res.cross(new Vec3d(vec1), new Vec3d(vec2));
    return res;
  }

  /**
   * Returns the distance between the two point from and to.
   * 
   * @param from
   *          the from point.
   * @param to
   *          the to point.
   * @return the distance between the two point from and to.
   */
  public static double distance(Vec3d from, Vec3d to) {
    return from.distance(to);
  }

  /**
   * Returns the distance squared between the two point from and to.
   * 
   * @param from
   *          the from point.
   * @param to
   *          the to point.
   * @return the distance squared between the two point from and to.
   */
  public static double distanceSquared(Vec3d from, Vec3d to) {
    return from.distanceSquared(to);
  }

  /**
   * Extracts the directional vectors from the specified view matrix.
   * 
   * @param matrix
   *          the view matrix.
   * @param upVecOut
   *          the up vector.
   * @param sideVecOut
   *          the side vector.
   * @param lookVecOut
   *          the look vector.
   */
  public static void getVectorsForMatrix(Mat4d matrix, Vec3d upVecOut, Vec3d sideVecOut, Vec3d lookVecOut) {

    sideVecOut.set(matrix.getElement(0, 0), matrix.getElement(0, 1), matrix.getElement(0, 2));
    sideVecOut.normalize();

    upVecOut.set(matrix.getElement(1, 0), matrix.getElement(1, 1), matrix.getElement(1, 2));
    upVecOut.normalize();

    lookVecOut.set(matrix.getElement(2, 0), matrix.getElement(2, 1), matrix.getElement(2, 2));
    lookVecOut.negate();
    lookVecOut.normalize();

  }

  /**
   * Extracts the up vector from the specified view matrix.
   * 
   * @param matrix
   *          the matrix.
   * @return the up vector from the specified view matrix.
   */

  public static Vec3d getUpFromMatrix(Mat4d matrix) {
    Vec3d res = new Vec3d(matrix.getElement(1, 0), matrix.getElement(1, 1), matrix.getElement(1, 2));
    res.normalize();
    return res;
  }

}
