package com.enderio.core.common.network;

import javax.annotation.Nonnull;

import com.enderio.core.api.common.util.IProgressTile;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketProgress extends PacketTileEntity<BlockEntity> {

  float progress;

  public PacketProgress() {
  }

  public PacketProgress(@Nonnull IProgressTile tile) {
    super(tile.getTileEntity());
    progress = tile.getProgress();
  }

  public PacketProgress(FriendlyByteBuf buffer) {
    super(buffer);
    progress = buffer.readFloat();
  }

  @Override
  public void toBytes(FriendlyByteBuf buffer) {
    super.toBytes(buffer);
    buffer.writeFloat(progress);
  }

  @Override
  public void onReceived(@Nonnull BlockEntity te, @Nonnull Supplier<NetworkEvent.Context> context) {
    if (te instanceof IProgressTile) {
      ((IProgressTile) te).setProgress(progress);
    }
  }
}
