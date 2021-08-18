package com.enderio.core.common.util;

import net.minecraft.util.Direction;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class NNSidedObject<T> {

  @SuppressWarnings("unchecked")
  private final @Nonnull T[] data = (T[]) new Object[6];

  public NNSidedObject(@Nonnull Function<Direction, T> defaultProvider) {
    NNList.FACING.apply((NNList.Callback<Direction>) side -> set(side, NullHelper.notnull(defaultProvider.apply(side), "internal logic error")));
  }

  public NNSidedObject(@Nonnull T defaultValue) {
    NNList.FACING.apply((NNList.Callback<Direction>) side -> set(side, defaultValue));
  }

  public @Nonnull T get(@Nonnull Direction side) {
    return NullHelper.notnull(data[side.ordinal()], "internal logic error");
  }

  /**
   * Set a new value and return it (for chaining).
   */
  public T set(@Nonnull Direction side, @Nonnull T value) {
    return data[side.ordinal()] = value;
  }

}
