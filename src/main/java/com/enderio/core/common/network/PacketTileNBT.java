package com.enderio.core.common.network;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.Log;
import com.enderio.core.common.util.NullHelper;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created by CrazyPants on 27/02/14.
 */
public class PacketTileNBT {

  BlockEntity te;

  long pos;
  CompoundTag tags;

  boolean renderOnUpdate = false;

  public PacketTileNBT() {

  }

  public PacketTileNBT(BlockEntity te) {
    this.te = te;
    pos = te.getBlockPos().asLong();
    te.save(tags = new CompoundTag());
  }

  public void toBytes(FriendlyByteBuf buffer) {
    buffer.writeLong(pos);
    buffer.writeNbt(tags);
  }

  public void fromBytes(FriendlyByteBuf buffer) {
    pos = buffer.readLong();
    tags = buffer.readNbt();
  }

  public @Nonnull BlockPos getPos() {
    return BlockPos.of(pos);
  }

  public boolean handle(Supplier<NetworkEvent.Context> context) {
    context.get().enqueueWork(() -> {
      te = handle(context.get().getSender().level);
      if (te != null && renderOnUpdate) {
        BlockState bs = te.getLevel().getBlockState(getPos());
        te.getLevel().sendBlockUpdated(getPos(), bs, bs, 3);
      }
    });
    return true;
  }

  private @Nullable BlockEntity handle(Level world) {
    if (world == null) {
      Log.warn("PacketUtil.handleTileEntityPacket: TileEntity null world processing tile entity packet.");
      return null;
    }
    BlockEntity tileEntity = world.getBlockEntity(getPos());
    if (tileEntity == null) {
      Log.warn("PacketUtil.handleTileEntityPacket: TileEntity null when processing tile entity packet.");
      return null;
    }
    tileEntity.load(NullHelper.notnull(tags, "NetworkUtil.readNBTTagCompound()"));
    return tileEntity;
  }
}
