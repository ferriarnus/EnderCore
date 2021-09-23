package com.enderio.core.common.interfaces;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;

public interface IUnderlayRenderAware {
  public void renderItemAndEffectIntoGUI(@Nonnull ItemStack stack, int xPosition, int yPosition);
}