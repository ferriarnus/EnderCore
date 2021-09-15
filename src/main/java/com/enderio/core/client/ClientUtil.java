package com.enderio.core.client;

import java.lang.reflect.Field;

import com.enderio.core.common.util.Log;

import net.minecraft.client.particle.Particle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

@OnlyIn(Dist.CLIENT)
public class ClientUtil {
  private static final Field MOTION_X;
  private static final Field MOTION_Y;
  private static final Field MOTION_Z;

  static {
    Field motionX = null;
    Field motionY = null;
    Field motionZ = null;
    try {
      motionX = ObfuscationReflectionHelper.findField(Particle.class, "field_187129_i");
      motionY = ObfuscationReflectionHelper.findField(Particle.class, "field_187130_j");
      motionZ = ObfuscationReflectionHelper.findField(Particle.class, "field_187131_k");
    } catch (Exception e) {
      Log.error("ClientUtil: Could not find motion fields for class Particle: " + e.getMessage());
    } finally {
      MOTION_X = motionX;
      MOTION_Y = motionY;
      MOTION_Z = motionZ;
    }
  }

  public static void setParticleVelocity(Particle p, double x, double y, double z) {
    if (p == null) {
      return;
    }

    try {
      MOTION_X.set(p, x);
      MOTION_Y.set(p, y);
      MOTION_Z.set(p, z);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void setParticleVelocityY(Particle p, double y) {
    if (p == null) {
      return;
    }

    try {
      MOTION_Y.set(p, y);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static double getParticleVelocityY(Particle p) {
    if (p == null) {
      return 0;
    }

    try {
      Object val = MOTION_Y.get(p);
      return ((Double) val).doubleValue();
    } catch (Exception e) {
      e.printStackTrace();
      return 0;
    }
  }
}
