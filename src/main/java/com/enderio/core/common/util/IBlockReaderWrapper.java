package com.enderio.core.common.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

public class IBlockReaderWrapper implements BlockGetter {

  protected @Nonnull BlockGetter wrapped;

  public IBlockReaderWrapper(@Nonnull BlockGetter ba) {
    wrapped = ba;
  }

  @Override
  public @Nullable BlockEntity getBlockEntity(@Nonnull BlockPos pos) {
    if (pos.getY() >= 0 && pos.getY() < 256) {
      return wrapped.getBlockEntity(pos);
    } else {
      return null;
    }
  }

  @Override
  public @Nonnull BlockState getBlockState(@Nonnull BlockPos pos) {
    return wrapped.getBlockState(pos);
  }

  @Override
  public FluidState getFluidState(BlockPos pos) {
    return wrapped.getFluidState(pos);
  }

  @Override
  public int getLightEmission(BlockPos pos) {
    return 15 << 20 | 15 << 4;
  }

  @Override
  public int getHeight() {
    return 0;
  }

  @Override
  public int getMinBuildHeight() {
    return 0;
  }
}
