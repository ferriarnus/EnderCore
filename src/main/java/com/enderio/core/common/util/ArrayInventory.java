package com.enderio.core.common.util;

import javax.annotation.Nonnull;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class ArrayInventory implements Container {

  protected final @Nonnull ItemStack[] items;

  public ArrayInventory(@Nonnull ItemStack[] items) {
    this.items = items;
  }

  public ArrayInventory(int size) {
    items = new ItemStack[size];
  }

  @Override
  public int getContainerSize() {
    return items.length;
  }

  @Override
  public @Nonnull ItemStack getItem(int slot) {
    final ItemStack itemStack = items[slot];
    return itemStack != null ? itemStack : ItemStack.EMPTY;
  }

  @Override
  public @Nonnull ItemStack removeItem(int slot, int amount) {
    return EnderUtil.decrStackSize(this, slot, amount);
  }

  @Override
  public void setItem(int slot, @Nonnull ItemStack stack) {
    items[slot] = stack;
    setChanged();
  }

  @Override
  public int getMaxStackSize() {
    return 64;
  }

  @Override
  public boolean stillValid(@Nonnull Player var1) {
    return true;
  }

  @Override
  public boolean canPlaceItem(int i, @Nonnull ItemStack itemstack) {
    return true;
  }

  @Override
  public void setChanged() {

  }

  /*@Override
  public @Nonnull String getName() {
    return "ArrayInventory";
  }

  @Override
  public boolean hasCustomName() {
    return false;
  }

  @Override
  public @Nonnull ITextComponent getDisplayName() {
    return new StringTextComponent(getName());
  }*/

  @Override
  public @Nonnull ItemStack removeItemNoUpdate(int index) {
    ItemStack res = items[index];
    items[index] = ItemStack.EMPTY;
    return res != null ? res : ItemStack.EMPTY;
  }

  @Override
  public void startOpen(@Nonnull Player player) {
  }

  @Override
  public void stopOpen(@Nonnull Player player) {
  }

  @Override
  public void clearContent() {
    for (int i = 0; i < items.length; i++) {
      items[i] = ItemStack.EMPTY;
    }

  }

  @Override
  public boolean isEmpty() {
    for (ItemStack itemstack : items) {
      if (itemstack != null && !itemstack.isEmpty()) {
        return false;
      }
    }
    return true;
  }

}
