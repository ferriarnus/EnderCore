package com.enderio.core.common.recipes;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Stream;

public class EnderIngredient extends Ingredient {
  int count;
  /**
   * For serialization purposes, so we only need to serialize count and the parent
   */
  @Nonnull
  Ingredient parent;

  boolean isCountUpdated = false;

  protected EnderIngredient(Stream<? extends IItemList> itemLists) {
    super(itemLists);
    parent = Ingredient.fromItemListStream(itemLists);
  }
  protected EnderIngredient(Ingredient ingredient) {
    super(Arrays.stream(ingredient.getMatchingStacks()).map(SingleItemList::new));
    this.parent = ingredient;
  }

  @Override
  public ItemStack[] getMatchingStacks() {
    ItemStack[] matchingStacks = super.getMatchingStacks();
    if (isCountUpdated)
      return matchingStacks;
    for (ItemStack matchingStack : matchingStacks) {
      matchingStack.setCount(count);
    }
    isCountUpdated = true;
    return matchingStacks;
  }

}
