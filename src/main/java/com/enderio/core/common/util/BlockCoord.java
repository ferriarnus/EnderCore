package com.enderio.core.common.util;

import javax.annotation.Nonnull;

import com.google.common.base.Strings;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.ChatFormatting;

public class BlockCoord {

  private BlockCoord() {
  }

  public static @Nonnull BlockPos get(BlockEntity tile) {
    return get(tile.getBlockPos());
  }

  public static @Nonnull BlockPos get(Entity e) {
    return get(e.getX(), e.getY(), e.getZ());
  }

  public static @Nonnull BlockPos get(BlockPos bc) {
    return get(bc.getX(), bc.getY(), bc.getZ());
  }

  public static @Nonnull BlockPos get(int x, int y, int z) {
    return new BlockPos(x, y, z);
  }

  private static @Nonnull BlockPos get(double x, double y, double z) {
    return get((int) x, (int) y, (int) z);
  }

  public static @Nonnull BlockPos get(String x, String y, String z) {
    return get(Strings.isNullOrEmpty(x) ? 0 : Integer.parseInt(x), Strings.isNullOrEmpty(y) ? 0 : Integer.parseInt(y),
        Strings.isNullOrEmpty(z) ? 0 : Integer.parseInt(z));
  }

  public static @Nonnull BlockPos get(BlockHitResult mop) {
    return get(mop.getBlockPos());
  }

  public static int getDistSq(BlockPos a, BlockPos b) {
    int xDiff = a.getX() - b.getX();
    int yDiff = a.getY() - b.getY();
    int zDiff = a.getZ() - b.getZ();
    return xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;
  }

  public static int getDistSq(BlockPos a, BlockEntity other) {
    return getDistSq(a, get(other));
  }

  public static int getDist(BlockPos a, BlockPos b) {
    double dsq = getDistSq(a, b);
    return (int) Math.ceil(Math.sqrt(dsq));
  }

  public static int getDist(BlockPos a, BlockEntity other) {
    return getDist(a, get(other));
  }

  public static @Nonnull String chatString(BlockPos pos, ChatFormatting defaultColor) {
    return String.format("x%s%d%s y%s%d%s z%s%d", ChatFormatting.GREEN, pos.getX(), defaultColor, ChatFormatting.GREEN, pos.getY(), defaultColor,
        ChatFormatting.GREEN, pos.getZ());
  }

  public static @Nonnull BlockPos withX(BlockPos pos, final int x) {
    return pos.getX() == x ? pos : get(x, pos.getY(), pos.getZ());
  }

  public static @Nonnull BlockPos withY(BlockPos pos, final int y) {
    return pos.getY() == y ? pos : get(pos.getX(), y, pos.getZ());
  }

  public static @Nonnull BlockPos withZ(BlockPos pos, final int z) {
    return pos.getZ() == z ? pos : get(pos.getX(), pos.getY(), z);
  }
}
