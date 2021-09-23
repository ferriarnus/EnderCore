package com.enderio.core.common.util;

import com.enderio.core.api.common.util.IProgressTile;
import com.enderio.core.common.vecmath.Vec3d;
import com.google.common.io.Files;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class EnderUtil {

  public static @Nullable Block getBlockFromItemId(@Nonnull ItemStack itemId) {
    Item item = itemId.getItem();
    if (item instanceof BlockItem) {
      return ((BlockItem) item).getBlock();
    }
    return null;
  }

  public static @Nonnull ItemStack consumeItem(@Nonnull ItemStack stack) {
    if (stack.getItem() instanceof PotionItem) {
      if (stack.getCount() == 1) {
        return new ItemStack(Items.GLASS_BOTTLE);
      } else {
        stack.split(1);
        return stack;
      }
    }
    if (stack.getCount() == 1) {
      if (stack.getItem().hasContainerItem(stack)) {
        return stack.getItem().getContainerItem(stack);
      } else {
        return ItemStack.EMPTY;
      }
    } else {
      stack.split(1);
      return stack;
    }
  }

  public static void giveExperience(@Nonnull Player thePlayer, float experience) {
    int intExp = (int) experience;
    float fractional = experience - intExp;
    if (fractional > 0.0F && (float) Math.random() < fractional) {
      ++intExp;
    }
    while (intExp > 0) {
      int j = ExperienceOrb.getExperienceValue(intExp);
      intExp -= j;
      thePlayer.level.addFreshEntity(new ExperienceOrb(thePlayer.level, thePlayer.getX(), thePlayer.getY() + 0.5D, thePlayer.getZ() + 0.5D, j));
    }
  }

  public static ItemEntity createDrop(@Nonnull Level world, @Nonnull ItemStack stack, double x, double y, double z, boolean doRandomSpread) {
    if (stack.isEmpty()) {
      return null;
    }
    if (doRandomSpread) {
      float f1 = 0.7F;
      double d = (world.random.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      double d1 = (world.random.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      double d2 = (world.random.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      ItemEntity entityitem = new ItemEntity(world, x + d, y + d1, z + d2, stack);
      entityitem.setDefaultPickUpDelay();
      return entityitem;
    } else {
      ItemEntity entityitem = new ItemEntity(world, x, y, z, stack);
      entityitem.setDeltaMovement(0, 0, 0);
      entityitem.setNoPickUpDelay();
      return entityitem;
    }
  }

  public static void dropItems(@Nonnull Level world, @Nonnull ItemStack stack, @Nonnull BlockPos pos, boolean doRandomSpread) {
    dropItems(world, stack, pos.getX(), pos.getY(), pos.getZ(), doRandomSpread);
  }

  public static void dropItems(@Nonnull Level world, @Nonnull ItemStack stack, double x, double y, double z, boolean doRandomSpread) {
    if (stack.isEmpty()) {
      return;
    }

    ItemEntity entityitem = createEntityItem(world, stack, x, y, z, doRandomSpread);
    world.addFreshEntity(entityitem);
  }

  public static ItemEntity createEntityItem(@Nonnull Level world, @Nonnull ItemStack stack, double x, double y, double z) {
    return createEntityItem(world, stack, x, y, z, true);
  }

  public static @Nonnull ItemEntity createEntityItem(@Nonnull Level world, @Nonnull ItemStack stack, double x, double y, double z, boolean doRandomSpread) {
    ItemEntity entityitem;
    if (doRandomSpread) {
      float f1 = 0.7F;
      double d = (world.random.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      double d1 = (world.random.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      double d2 = (world.random.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      entityitem = new ItemEntity(world, x + d, y + d1, z + d2, stack);
      entityitem.setDefaultPickUpDelay();
    } else {
      entityitem = new ItemEntity(world, x, y, z, stack);
      entityitem.setDeltaMovement(0, 0, 0);
      entityitem.setNoPickUpDelay();
    }
    return entityitem;
  }

  public static void dropItems(@Nonnull Level world, @Nonnull ItemStack stack, int x, int y, int z, boolean doRandomSpread) {
    if (stack.isEmpty()) {
      return;
    }

    if (doRandomSpread) {
      float f1 = 0.7F;
      double d = (world.random.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      double d1 = (world.random.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      double d2 = (world.random.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      ItemEntity entityitem = new ItemEntity(world, x + d, y + d1, z + d2, stack);
      entityitem.setDefaultPickUpDelay();
      world.addFreshEntity(entityitem);
    } else {
      ItemEntity entityitem = new ItemEntity(world, x + 0.5, y + 0.5, z + 0.5, stack);
      entityitem.setDeltaMovement(0,0,0);
      entityitem.setNoPickUpDelay();
      world.addFreshEntity(entityitem);
    }
  }

  public static void dropItems(@Nonnull Level world, ItemStack[] inventory, int x, int y, int z, boolean doRandomSpread) {
    if (inventory == null) {
      return;
    }
    for (ItemStack stack : inventory) {
      if (!stack.isEmpty()) {
        dropItems(world, stack.copy(), x, y, z, doRandomSpread);
      }
    }
  }

  public static void dropItems(@Nonnull Level world, @Nonnull Container inventory, int x, int y, int z, boolean doRandomSpread) {
    for (int l = 0; l < inventory.getContainerSize(); ++l) {
      ItemStack items = inventory.getItem(l);

      if (!items.isEmpty()) {
        dropItems(world, items.copy(), x, y, z, doRandomSpread);
      }
    }
  }

  public static boolean dumpModObjects(@Nonnull File file) {

    StringBuilder sb = new StringBuilder();
    for (Object key : ForgeRegistries.BLOCKS.getKeys()) {
      if (key != null) {
        sb.append(key.toString());
        sb.append("\n");
      }
    }
    for (Object key : ForgeRegistries.ITEMS.getKeys()) {
      if (key != null) {
        sb.append(key.toString());
        sb.append("\n");
      }
    }

    try {
      Files.write(sb, file, StandardCharsets.UTF_8);
      return true;
    } catch (IOException e) {
      Log.warn("Error dumping ore dictionary entries: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  public static @Nonnull ItemStack decrStackSize(@Nonnull Container inventory, int slot, int size) {
    ItemStack item = inventory.getItem(slot);
    if (!item.isEmpty()) {
      if (item.getCount() <= size) {
        ItemStack result = item;
        inventory.setItem(slot, ItemStack.EMPTY);
        inventory.setChanged();
        return result;
      }
      ItemStack split = item.split(size);
      inventory.setChanged();
      return split;
    }
    return ItemStack.EMPTY;
  }

  public static @Nonnull Vec3 getEyePosition(@Nonnull Player player) {
    double y = player.getY();
    y += player.getEyeHeight();
    return new Vec3(player.getX(), y, player.getZ());
  }

  public static @Nonnull Vec3d getEyePositionEio(@Nonnull Player player) {
    Vec3d res = new Vec3d(player.getX(), player.getY(), player.getZ());
    res.y += player.getEyeHeight();
    return res;
  }

  public static @Nonnull Vec3d getLookVecEio(@Nonnull Player player) {
    Vec3 lv = player.getLookAngle();
    return new Vec3d(lv.x, lv.y, lv.z);
  }

  // Code adapted from World.rayTraceBlocks to return all
  // collided blocks
  public static @Nonnull List<BlockHitResult> raytraceAll(@Nonnull Level world, ClipContext context) {
    return doRayTrace(context, (p_217297_1_, p_217297_2_) -> {
      BlockState blockstate = world.getBlockState(p_217297_2_);
      FluidState fluidstate = world.getFluidState(p_217297_2_);
      Vec3 vector3d = p_217297_1_.getFrom();
      Vec3 vector3d1 = p_217297_1_.getTo();
      VoxelShape voxelshape = p_217297_1_.getBlockShape(blockstate, world, p_217297_2_);
      BlockHitResult blockraytraceresult = world.clipWithInteractionOverride(vector3d, vector3d1, p_217297_2_, voxelshape, blockstate);
      VoxelShape voxelshape1 = p_217297_1_.getFluidShape(fluidstate, world, p_217297_2_);
      BlockHitResult blockraytraceresult1 = voxelshape1.clip(vector3d, vector3d1, p_217297_2_);
      double d0 = blockraytraceresult == null ? Double.MAX_VALUE : p_217297_1_.getFrom().distanceToSqr(blockraytraceresult.getLocation());
      double d1 = blockraytraceresult1 == null ? Double.MAX_VALUE : p_217297_1_.getFrom().distanceToSqr(blockraytraceresult1.getLocation());
      return d0 <= d1 ? blockraytraceresult : blockraytraceresult1;
    }, (p_217302_0_) -> {
      Vec3 vector3d = p_217302_0_.getFrom().subtract(p_217302_0_.getTo());
      return BlockHitResult.miss(p_217302_0_.getTo(), Direction.getNearest(vector3d.x, vector3d.y, vector3d.z), new BlockPos(p_217302_0_.getTo()));
    });
  }

  private static List<BlockHitResult> doRayTrace(ClipContext context, BiFunction<ClipContext, BlockPos, BlockHitResult> rayTracer, Function<ClipContext, BlockHitResult> missFactory) {
    List<BlockHitResult> result = new ArrayList<>();

    Vec3 vector3d = context.getFrom();
    Vec3 vector3d1 = context.getTo();
    if (vector3d.equals(vector3d1)) {
      return result;
    } else {
      double d0 = Mth.lerp(-1.0E-7D, vector3d1.x, vector3d.x);
      double d1 = Mth.lerp(-1.0E-7D, vector3d1.y, vector3d.y);
      double d2 = Mth.lerp(-1.0E-7D, vector3d1.z, vector3d.z);
      double d3 = Mth.lerp(-1.0E-7D, vector3d.x, vector3d1.x);
      double d4 = Mth.lerp(-1.0E-7D, vector3d.y, vector3d1.y);
      double d5 = Mth.lerp(-1.0E-7D, vector3d.z, vector3d1.z);
      int i = Mth.floor(d3);
      int j = Mth.floor(d4);
      int k = Mth.floor(d5);
      BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos(i, j, k);
      BlockHitResult t = rayTracer.apply(context, blockpos$mutable);
      if (t != null) {
        result.add(t);
      } else {
        double d6 = d0 - d3;
        double d7 = d1 - d4;
        double d8 = d2 - d5;
        int l = Mth.sign(d6);
        int i1 = Mth.sign(d7);
        int j1 = Mth.sign(d8);
        double d9 = l == 0 ? Double.MAX_VALUE : (double)l / d6;
        double d10 = i1 == 0 ? Double.MAX_VALUE : (double)i1 / d7;
        double d11 = j1 == 0 ? Double.MAX_VALUE : (double)j1 / d8;
        double d12 = d9 * (l > 0 ? 1.0D - Mth.frac(d3) : Mth.frac(d3));
        double d13 = d10 * (i1 > 0 ? 1.0D - Mth.frac(d4) : Mth.frac(d4));
        double d14 = d11 * (j1 > 0 ? 1.0D - Mth.frac(d5) : Mth.frac(d5));

        while(d12 <= 1.0D || d13 <= 1.0D || d14 <= 1.0D) {
          if (d12 < d13) {
            if (d12 < d14) {
              i += l;
              d12 += d9;
            } else {
              k += j1;
              d14 += d11;
            }
          } else if (d13 < d14) {
            j += i1;
            d13 += d10;
          } else {
            k += j1;
            d14 += d11;
          }

          BlockHitResult t1 = rayTracer.apply(context, blockpos$mutable.set(i, j, k));
          if (t1 != null) {
            result.add(t1);
          }
        }

        if (result.isEmpty())
          result.add(missFactory.apply(context));
      }
    }

    return result;
  }

  public static @Nullable Direction getDirFromOffset(int xOff, int yOff, int zOff) {
    if (xOff != 0 && yOff == 0 && zOff == 0) {
      return xOff < 0 ? Direction.WEST : Direction.EAST;
    }
    if (zOff != 0 && yOff == 0 && xOff == 0) {
      return zOff < 0 ? Direction.NORTH : Direction.SOUTH;
    }
    if (yOff != 0 && xOff == 0 && zOff == 0) {
      return yOff < 0 ? Direction.DOWN : Direction.UP;
    }
    return null;
  }

  public static @Nonnull Direction getFacingFromEntity(@Nonnull LivingEntity entity) {
    int heading = Mth.floor(entity.yRotO * 4.0F / 360.0F + 0.5D) & 3;
    switch (heading) {
    case 0:
      return Direction.NORTH;
    case 1:
      return Direction.EAST;
    case 2:
      return Direction.SOUTH;
    case 3:
    default:
      return Direction.WEST;
    }

  }

  public static int getProgressScaled(int scale, @Nonnull IProgressTile tile) {
    return (int) (tile.getProgress() * scale);
  }

  public static void writeFacingToNBT(@Nonnull CompoundTag nbtRoot, @Nonnull String name, @Nonnull Direction dir) {
    short val = -1;
    val = (short) dir.ordinal();
    nbtRoot.putShort(name, val);
  }

  public static @Nullable Direction readFacingFromNBT(@Nonnull CompoundTag nbtRoot, @Nonnull String name) {
    short val = -1;
    if (nbtRoot.contains(name)) {
      val = nbtRoot.getShort(name);
    }
    if (val > 0) {
      return Direction.values()[val];
    }
    return null;
  }

}
