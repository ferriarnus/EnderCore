package com.enderio.core.api.client.gui;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

public interface IAdvancedTooltipProvider {

  default void addCommonEntries(@Nonnull ItemStack itemstack, @Nullable Player entityplayer, @Nonnull List<Component> list, boolean flag) {
  }

  default void addBasicEntries(@Nonnull ItemStack itemstack, @Nullable Player entityplayer, @Nonnull List<Component> list, boolean flag) {
  }

  default void addDetailedEntries(@Nonnull ItemStack itemstack, @Nullable Player entityplayer, @Nonnull List<Component> list, boolean flag) {
  }

}
