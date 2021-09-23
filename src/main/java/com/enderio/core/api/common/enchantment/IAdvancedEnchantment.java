package com.enderio.core.api.common.enchantment;

import javax.annotation.Nonnull;

import com.enderio.core.EnderCore;
import com.google.common.base.Predicate;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Allows your enchants to have some flavor or description text underneath them
 */
public interface IAdvancedEnchantment {
  public static final EnchantmentCategory ALL = EnchantmentCategory.create("EC_REALLY_ALL", (Predicate<Item>) input -> true);

  /**
   * Get the detail for this itemstack
   *
   * @param stack
   * @return a list of <code>String</code>s to be bulleted under the enchantment
   */
  default @Nonnull String[] getTooltipDetails(@Nonnull ItemStack stack) {
    final String unloc = "description." + ((Enchantment) this).getDescriptionId();
    final String loc = EnderCore.lang.localizeExact(unloc);
    return unloc.equals(loc) ? new String[0] : EnderCore.lang.splitList(loc);
  }

}
