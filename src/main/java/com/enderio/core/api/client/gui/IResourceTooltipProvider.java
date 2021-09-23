package com.enderio.core.api.client.gui;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;

public interface IResourceTooltipProvider {

  @Nonnull
  String getTranslationKeyForTooltip(@Nonnull ItemStack itemStack);

}
