package com.enderio.core.api.common.util;

import javax.annotation.Nonnull;

import net.minecraft.network.IPacket;
import net.minecraft.tileentity.TileEntity;

public interface IProgressTile {

  float getProgress();

  /**
   * Client-only. Called to set clientside progress for syncing/rendering purposes.
   *
   * @param progress
   *          The % progress.
   */
  void setProgress(float progress);

  @Nonnull
  TileEntity getTileEntity();

  @Nonnull
  IPacket getProgressPacket();

}
