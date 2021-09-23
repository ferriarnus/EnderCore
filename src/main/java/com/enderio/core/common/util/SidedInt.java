package com.enderio.core.common.util;

import net.minecraft.core.Direction;

import javax.annotation.Nonnull;

public class SidedInt {

  private final @Nonnull int[] data = new int[6];

  public SidedInt() {
    this(0);
  }

  public SidedInt(int defaultValue) {
    NNList.FACING.apply(side -> set(side, defaultValue));
  }

  public int get(@Nonnull Direction side) {
    return data[side.ordinal()];
  }

  /**
   * Set a new value and return it (for chaining).
   */
  public int set(@Nonnull Direction side, int value) {
    return data[side.ordinal()] = value;
  }

}
