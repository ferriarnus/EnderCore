package com.enderio.core.common.util;

import java.util.Random;

import javax.annotation.Nonnull;

import com.enderio.core.EnderCore;
import com.enderio.core.common.vecmath.Vec3d;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityUtil {

  private static final Random rand = new Random();

  public static void setEntityVelocity(Entity entity, double velX, double velY, double velZ) {
    entity.setDeltaMovement(velX, velY, velZ);
  }

  public static @Nonnull FireworkRocketEntity getRandomFirework(@Nonnull Level world) {
    return getRandomFirework(world, new BlockPos(0, 0, 0));
  }

  public static @Nonnull FireworkRocketEntity getRandomFirework(@Nonnull Level world, @Nonnull BlockPos pos) {
    ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
    firework.setTag(new CompoundTag());
    CompoundTag expl = new CompoundTag();
    expl.putBoolean("Flicker", true);
    expl.putBoolean("Trail", true);

    int[] colors = new int[rand.nextInt(8) + 1];
    for (int i = 0; i < colors.length; i++) {
      colors[i] = DyeColor.values()[rand.nextInt(16)].getFireworkColor();
    }
    expl.putIntArray("Colors", colors);
    byte type = (byte) (rand.nextInt(3) + 1);
    type = type == 3 ? 4 : type;
    expl.putByte("Type", type);

    ListTag explosions = new ListTag();
    explosions.add(expl);

    CompoundTag fireworkTag = new CompoundTag();
    fireworkTag.put("Explosions", explosions);
    fireworkTag.putByte("Flight", (byte) 1);
    firework.addTagElement("Fireworks", fireworkTag);

    FireworkRocketEntity e = new FireworkRocketEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, firework);
    return e;
  }

  public static void spawnFirework(@Nonnull BlockPos block, Level world) {
    spawnFirework(block, world, 0);
  }

  public static void spawnFirework(@Nonnull BlockPos pos, Level world, int range) {
    BlockPos spawnPos = pos;

    // don't bother if there's no randomness at all
    if (range > 0) {
      for (int i = 0; i < 100; i++) {
        spawnPos = new BlockPos(moveRandomly(spawnPos.getX(), range), spawnPos.getY(), moveRandomly(spawnPos.getZ(), range));
        BlockState bs = world.getBlockState(spawnPos);
        if (world.isEmptyBlock(new BlockPos(spawnPos)) || bs.getMaterial().isReplaceable()) { // Vanillas isReplaceable is dumb.
          break;
        }
      }
    }

    world.addFreshEntity(getRandomFirework(world, spawnPos));
  }

  private static double moveRandomly(double base, double range) {
    return base + 0.5 + rand.nextDouble() * range - (range / 2);
  }

  public static @Nonnull String getDisplayNameForEntity(@Nonnull String mobName) {
    return EnderCore.lang.localizeExact("entity." + mobName + ".name");
  }

  public static @Nonnull NNList<ResourceLocation> getAllRegisteredMobNames() {
    NNList<ResourceLocation> result = new NNList<ResourceLocation>();
    for (ResourceLocation entityName : ForgeRegistries.ENTITIES.getKeys()) {
      EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(entityName);
      if (entityType != null && entityType.getCategory() != MobCategory.MISC) {
        result.add(entityName);
      }
    }
    return result;
  }

  public static boolean isRegisteredMob(ResourceLocation entityName) {
    if (entityName != null) {
      EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(entityName);
      return entityType != null && entityType.getCategory() != MobCategory.MISC;
    }
    return false;
  }

  private EntityUtil() {
  }

  public static Vec3d getEntityPosition(@Nonnull Entity ent) {
    return new Vec3d(ent.getX(), ent.getY(), ent.getZ());
  }

  public static void spawnItemInWorldWithRandomMotion(@Nonnull Level world, @Nonnull ItemStack item, int x, int y, int z) {
    if (!item.isEmpty()) {
      spawnItemInWorldWithRandomMotion(world, item, x + 0.5, y + 0.5, z + 0.5);
    }
  }

  public static void spawnItemInWorldWithRandomMotion(@Nonnull Level world, @Nonnull ItemStack item, double x, double y, double z) {
    if (!item.isEmpty()) {
      spawnItemInWorldWithRandomMotion(new ItemEntity(world, x, y, z, item));
    }
  }

  public static void spawnItemInWorldWithRandomMotion(@Nonnull ItemEntity entity) {
    entity.setDefaultPickUpDelay();

    float f = (entity.level.random.nextFloat() * 0.1f) - 0.05f;
    float f1 = (entity.level.random.nextFloat() * 0.1f) - 0.05f;
    float f2 = (entity.level.random.nextFloat() * 0.1f) - 0.05f;

    entity.setDeltaMovement(f, f1, f2);

    entity.level.addFreshEntity(entity);
  }
}
