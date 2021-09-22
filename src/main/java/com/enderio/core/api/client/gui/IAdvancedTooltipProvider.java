package com.enderio.core.api.client.gui;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public interface IAdvancedTooltipProvider {

  default void addCommonEntries(@Nonnull ItemStack itemstack, @Nullable PlayerEntity entityplayer, @Nonnull List<ITextComponent> list, boolean flag) {
  }

  default void addBasicEntries(@Nonnull ItemStack itemstack, @Nullable PlayerEntity entityplayer, @Nonnull List<ITextComponent> list, boolean flag) {
  }

  default void addDetailedEntries(@Nonnull ItemStack itemstack, @Nullable PlayerEntity entityplayer, @Nonnull List<ITextComponent> list, boolean flag) {
  }

}
