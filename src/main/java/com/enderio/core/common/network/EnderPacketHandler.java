package com.enderio.core.common.network;

import com.enderio.core.EnderCore;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EnderPacketHandler {
  private static SimpleChannel INSTANCE;
  private static int ID = 0;

  public static void init() {
    INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(EnderCore.MODID, "ender_channel"),
        () -> "1.0",
        s -> true,
        s -> true);

    registerClientMessage(PacketProgress.class, PacketProgress::toBytes, PacketProgress::new, PacketProgress::handle);
    registerServerMessage(PacketGhostSlot.class, PacketGhostSlot::toBytes, PacketGhostSlot::new, PacketGhostSlot::handle);
  }

  protected static <T> void registerClientMessage(Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder,
                                                                       BiConsumer<T, Supplier<NetworkEvent.Context>> consumer) {
    INSTANCE.registerMessage(ID++, type, encoder, decoder, consumer, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
  }

  protected static <T> void registerServerMessage(Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder,
                                                  BiConsumer<T, Supplier<NetworkEvent.Context>> consumer) {
    INSTANCE.registerMessage(ID++, type, encoder, decoder, consumer, Optional.of(NetworkDirection.PLAY_TO_SERVER));
  }

  public static void sendToAllTracking(Packet<?> message, BlockEntity te) {
    sendToAllTracking(message, te.getLevel(), te.getBlockPos());
  }

  // Credit: https://github.com/mekanism/Mekanism/blob/0287e5fd48a02dd8fe0b7a474c766d6c3a8d3f01/src/main/java/mekanism/common/network/BasePacketHandler.java#L150
  public static void sendToAllTracking(Packet<?> packet, Level world, BlockPos pos) {
    if (world instanceof ServerLevel) {
      ((ServerLevel) world).getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false).forEach(p -> sendTo(packet, p));
    } else{
      INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunk(pos.getX() >> 4, pos.getZ() >> 4)), packet);
    }
  }

  public static <T> void sendTo(T packet, ServerPlayer player) {
    INSTANCE.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
  }

  public static <T> void sendToServer(T packet) {
    INSTANCE.sendToServer(packet);
  }
}
