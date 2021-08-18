package com.enderio.core.common.util;

import net.minecraft.util.Direction;

import javax.annotation.Nonnull;

public class SidedObject<T> {

  @SuppressWarnings("unchecked")
  private final @Nonnull T[] data = (T[]) new Object[6];

  public SidedObject() {
    this(null);
  }

  public SidedObject(T defaultValue) {
    NNList.FACING.apply(side -> set(side, defaultValue));
  }

  public T get(@Nonnull Direction side) {
    return data[side.ordinal()];
  }

  /**
   * Set a new value and return it (for chaining).
   */
  public T set(@Nonnull Direction side, T value) {
    return data[side.ordinal()] = value;
  }

}
