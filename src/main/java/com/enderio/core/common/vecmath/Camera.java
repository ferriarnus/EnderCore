package com.enderio.core.common.vecmath;

import java.awt.Rectangle;

public class Camera {

  private Rectangle viewport;

  private Mat4d projectionTranspose;
  private Mat4d projectionMatrix;
  private Mat4d projectionInverse;

  private Mat4d viewTranspose;
  private Mat4d viewMatrix;
  private Mat4d viewInverse;

  public boolean isValid() {
    return viewMatrix != null && projectionMatrix != null && viewport != null;
  }

  public void setProjectionMatrixAsPerspective(double fovDegrees, double near, double far, int viewportWidth, int viewportHeight) {
    setProjectionMatrix(VecmathUtil.createProjectionMatrixAsPerspective(fovDegrees, near, far, viewportWidth, viewportHeight));
  }

  public void setViewMatrixAsLookAt(Vec3d eyePos, Vec3d lookAtPos, Vec3d upVec) {
    setViewMatrix(VecmathUtil.createMatrixAsLookAt(eyePos, lookAtPos, upVec));
  }

  public Vec3d getEyePoint() {
    Mat4d vpm = new Mat4d();
    Mat4d ivm = getInverseViewMatrix();
    if (ivm == null) {
      return null;
    }
    Mat4d ipm = getInverseProjectionMatrix();
    if (ipm == null) {
      return null;
    }
    vpm.mul(ivm, ipm);

    Vec3d eye = new Vec3d();
    ivm.getTranslation(eye);
    return eye;
  }

  public boolean getRayForPixel(int x, int y, Vec3d eyeOut, Vec3d normalOut) {
    if (isValid()) {
      VecmathUtil.computeRayForPixel(viewport, getInverseProjectionMatrix(), getInverseViewMatrix(), x, y, eyeOut, normalOut);
      return true;
    }
    return false;
  }

  public Vec2d getScreenPoint(Vec3d point3d) {
    Vec4d transPoint = new Vec4d(point3d.x, point3d.y, point3d.z, 1);

    viewMatrix.transform(transPoint);
    projectionMatrix.transform(transPoint);

    int halfWidth = viewport.width / 2;
    int halfHeight = viewport.height / 2;
    Vec2d screenPos = new Vec2d(transPoint.x, transPoint.y);
    screenPos.scale(1 / transPoint.w);
    screenPos.x = screenPos.x * halfWidth + halfWidth;
    screenPos.y = -screenPos.y * halfHeight + halfHeight;

    return screenPos;
  }

  public void setViewport(Rectangle viewport) {
    if (viewport != null) {
      setViewport(viewport.x, viewport.y, viewport.width, viewport.height);
    }
  }

  public void setViewport(int x, int y, int width, int height) {
    viewport = new Rectangle(x, y, width, height);
  }

  public Rectangle getViewport() {
    return viewport;
  }

  public Mat4d getProjectionMatrix() {
    return projectionMatrix;
  }

  public Mat4d getTransposeProjectionMatrix() {
    return projectionTranspose;
  }

  public Mat4d getInverseProjectionMatrix() {
    if (projectionMatrix != null) {
      if (projectionInverse == null) {
        projectionInverse = new Mat4d(projectionMatrix);
        projectionInverse.invert();
      }
      return projectionInverse;
    } else {
      return null;
    }
  }

  public void setProjectionMatrix(Mat4d matrix) {
    if (projectionMatrix == null) {
      projectionMatrix = new Mat4d();
      projectionTranspose = new Mat4d();
    }
    projectionMatrix.set(matrix);
    projectionTranspose.set(matrix);
    projectionTranspose.transpose();
    projectionInverse = null;
  }

  public Mat4d getViewMatrix() {
    return viewMatrix;
  }

  public Mat4d getTransposeViewMatrix() {
    return viewTranspose;
  }

  public Mat4d getInverseViewMatrix() {
    if (viewMatrix != null) {
      if (viewInverse == null) {
        viewInverse = new Mat4d(viewMatrix);
        viewInverse.invert();
      }
      return viewInverse;
    } else {
      return null;
    }
  }

  public void setViewMatrix(Mat4d matrix) {
    if (viewMatrix == null) {
      viewMatrix = new Mat4d();
      viewTranspose = new Mat4d();
    }
    viewMatrix.set(matrix);
    viewTranspose.set(matrix);
    viewTranspose.transpose();
    viewInverse = null;
  }

}
