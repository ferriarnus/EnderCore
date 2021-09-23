package com.enderio.core.common.util;

import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

/**
 * A mutable {@link BlockPos} that can point to a block and any of its 6 neighbours.
 *
 * Usage pattern:
 *
 * <pre>
 * Neighbours base = new Neighbours(new BlockPos(1, 2, 3));
 * for (BlockPos offset : base) { // base === offset
 *   world.getTileEntity(base).hasCapability(CAP, base.getOffset());
 * }
 * </pre>
 *
 */
public class Neighbours extends BlockPos implements Iterable<Neighbours>, Iterator<Neighbours> {

  private int x, y, z, offset = -1;

  public Neighbours(@Nonnull BlockPos source) {
    super(source);
    setOffset(null);
  }

  @Override
  public int getX() {
    return x;
  }

  @Override
  public int getY() {
    return y;
  }

  @Override
  public int getZ() {
    return z;
  }

  public void setOffset(@Nullable Direction facing) {
    if (facing != null) {
      x = super.getX() + facing.getStepX();
      y = super.getY() + facing.getStepY();
      z = super.getZ() + facing.getStepZ();
      offset = facing.ordinal();
    } else {
      x = super.getX();
      y = super.getY();
      z = super.getZ();
      offset = -1;
    }
  }

  /**
   * If the position was shifted it gives the direction of the shift. If not, it throws an {@link ArrayIndexOutOfBoundsException} to be {@link Nonnull}.
   */
  @SuppressWarnings("null") // Trust me, when Direction.VALUES contains a null you want to crash ASAP...
  public @Nonnull Direction getOffset() {
    return Direction.values()[offset];
  }

  /**
   * If the position was shifted it gives the direction opposite to the shift. If not, it throws an {@link ArrayIndexOutOfBoundsException} to be
   * {@link Nonnull}.
   */
  public @Nonnull Direction getOpposite() {
    return Direction.values()[offset].getOpposite();
  }

  @Override
  public @Nonnull BlockPos immutable() {
    return new BlockPos(this);
  }

  /**
   * See {@link Iterable#iterator()}. Please note that next() will always return the base {@link Neighbours} object which also means that iterators are not
   * independent.
   */
  @Override
  public Iterator<Neighbours> iterator() {
    offset = -1;
    return this;
  }

  @Override
  public boolean hasNext() {
    return offset < 5;
  }

  @Override
  public @Nonnull Neighbours next() {
    setOffset(Direction.values()[offset + 1]);
    return this;
  }

}

