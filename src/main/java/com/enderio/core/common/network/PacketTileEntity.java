package com.enderio.core.common.network;

import com.enderio.core.EnderCore;
import com.google.common.reflect.TypeToken;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.lang.reflect.TypeVariable;
import java.util.function.Supplier;

public abstract class PacketTileEntity<T extends BlockEntity> {

  private long pos;

  protected PacketTileEntity() {
  }

  protected PacketTileEntity(@Nonnull T tile) {
    pos = tile.getBlockPos().asLong();
  }

  public PacketTileEntity(FriendlyByteBuf buffer) {
    pos = buffer.readLong();
  }

  public void toBytes(FriendlyByteBuf buffer) {
    buffer.writeLong(pos);
  }

  public @Nonnull BlockPos getPos() {
    return BlockPos.of(pos);
  }

  @SuppressWarnings("unchecked")
  protected T getTileEntity(Level worldObj) {
    // Sanity check, and prevent malicious packets from loading chunks
    if (worldObj == null || !worldObj.hasChunkAt(getPos())) {
      return null;
    }
    BlockEntity te = worldObj.getBlockEntity(getPos());
    if (te == null) {
      return null;
    }
    @SuppressWarnings("rawtypes")
    final Class<? extends PacketTileEntity> ourClass = getClass();
    @SuppressWarnings("rawtypes")
    final TypeVariable<Class<PacketTileEntity>>[] typeParameters = PacketTileEntity.class.getTypeParameters();
    if (typeParameters.length > 0) {
      @SuppressWarnings("rawtypes")
      final TypeVariable<Class<PacketTileEntity>> typeParam0 = typeParameters[0];
      if (typeParam0 != null) {
        TypeToken<?> teType = TypeToken.of(ourClass).resolveType(typeParam0);
        final Class<? extends BlockEntity> teClass = te.getClass();
        if (teType.isSupertypeOf(teClass)) {
          return (T) te;
        }
      }
    }
    return null;
  }

  public boolean handle(Supplier<NetworkEvent.Context> context) {
    context.get().enqueueWork(() -> {
      if (context.get() != null) {
        T te = getTileEntity(getWorld(context));
        if (te != null) {
          onReceived(te, context);
        }
      }
    });
    return true;
  }

  public void onReceived(@Nonnull T te, @Nonnull Supplier<NetworkEvent.Context> context) {
  }

  protected @Nonnull Level getWorld(Supplier<NetworkEvent.Context> context) {
    if (context.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
      return context.get().getSender().level;
    } else {
      final Level clientWorld = EnderCore.proxy.getClientWorld();
      if (clientWorld == null) {
        throw new NullPointerException("Recieved network packet ouside any world!");
      }
      return clientWorld;
    }
  }
}
