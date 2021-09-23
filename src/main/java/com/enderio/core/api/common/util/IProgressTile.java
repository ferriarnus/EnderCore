package com.enderio.core.api.common.util;

import javax.annotation.Nonnull;

import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.entity.BlockEntity;

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
  BlockEntity getTileEntity();

  @Nonnull
  Packet getProgressPacket();

}
