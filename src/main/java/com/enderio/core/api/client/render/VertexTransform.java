package com.enderio.core.api.client.render;

import javax.annotation.Nonnull;

import com.enderio.core.common.vecmath.Vec3d;
import com.enderio.core.common.vecmath.Vec3f;
import com.enderio.core.common.vecmath.Vertex;

public interface VertexTransform {

  void apply(@Nonnull Vertex vertex);

  void apply(@Nonnull Vec3d vec);

  void applyToNormal(@Nonnull Vec3f vec);

}
