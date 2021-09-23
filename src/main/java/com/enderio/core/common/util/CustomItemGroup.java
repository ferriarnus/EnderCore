package com.enderio.core.common.util;

import javax.annotation.Nonnull;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CustomItemGroup extends CreativeModeTab {

  private @Nonnull ItemStack displayStack = ItemStack.EMPTY;

  public CustomItemGroup(@Nonnull String label) {
    super(label);
  }

  /**
   * @param item
   *          Item to display
   */
  public CustomItemGroup setDisplay(@Nonnull Item item) {
    return setDisplay(item, 0);
  }

  /**
   * @param item
   *          Item to display
   * @param damage
   *          Damage of item to display
   */
  public CustomItemGroup setDisplay(@Nonnull Item item, int damage) {
    return setDisplay(new ItemStack(item, 1));
  }

  /**
   * @param display
   *          ItemStack to display
   */
  public CustomItemGroup setDisplay(@Nonnull ItemStack display) {
    this.displayStack = display.copy();
    return this;
  }

  @Override
  public ItemStack makeIcon() {
    return displayStack;
  }
}
