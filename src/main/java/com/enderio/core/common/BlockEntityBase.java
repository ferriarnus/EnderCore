package com.enderio.core.common;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.common.util.IProgressTile;
import com.enderio.core.common.network.EnderPacketHandler;
import com.enderio.core.common.network.PacketProgress;
import com.enderio.core.common.util.NullHelper;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.ServerLevel;

import java.util.stream.Collectors;

public abstract class BlockEntityBase extends BlockEntity {

  private final int checkOffset = (int) (Math.random() * 20);
  protected final boolean isProgressTile;

  protected float lastProgressSent = -1;
  protected long lastProgressUpdate;
  private long lastUpdate = 0;

  public BlockEntityBase(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
    super(tileEntityTypeIn, pos, state);
    isProgressTile = this instanceof IProgressTile;
  }

  @Nullable
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level levelIn, BlockState blockStateIn, BlockEntityType<T> blockEntityType) {
    if (level.isClientSide()) {
      return null;
    } else {
      return (level1, pos, state1, tile) -> {
        if (tile instanceof BlockEntityBase teBase) {
          teBase.tickServer(state1);
        }
      };
    }
  }

  public void tickServer(BlockState state) {
    if (level.getBlockEntity(getBlockPos()) != this
            || level.getBlockState(worldPosition).getBlock() != state.getBlock()) {
      // we can get ticked after being removed from the world, ignore this
      return;
    }

    // TODO: Config:
//    if (ConfigHandler.allowExternalTickSpeedup || world.getGameTime() != lastUpdate) {
    if (level.getGameTime() != lastUpdate) {
      lastUpdate = level.getGameTime();
      doUpdate();
      sendProgressIf();
    }
  }

  public static int getProgressScaled(int scale, @Nonnull IProgressTile tile) {
    return (int) (tile.getProgress() * scale);
  }

  private final void sendProgressIf() {
    // this is only used for players that do not have the GUI open. They do not need a very fine resolution, as they only see the the machine being on or
    // off and get the sound restarted on progress==0
    if (isProgressTile && !level.isClientSide) {
      float progress = ((IProgressTile) this).getProgress();
      boolean send = //
          progress < lastProgressSent // always send progress if it goes down, e.g. machine goes inactive or new task starts
              || (lastProgressSent <= 0 && progress > 0) // always send progress if machine goes active
              || (lastUpdate - lastProgressUpdate) > 60 * 20; // also update every 60 seconds to avoid stale client status

      if (send) {
        EnderPacketHandler.sendToAllTracking(((IProgressTile) this).getProgressPacket(), this);
        lastProgressSent = progress;
        lastProgressUpdate = lastUpdate;
      }
    }
  }

  protected void doUpdate() {

  }

  public @Nonnull PacketProgress getProgressPacket() {
    return new PacketProgress((IProgressTile) this);
  }

  /**
   * SERVER: Called when being written to the save file.
   */
  @Override
  public final @Nonnull CompoundTag save(@Nonnull CompoundTag root) {
    super.save(root);
    writeCustomNBT(NBTAction.SAVE, root);
    return root;
  }

  /**
   * SERVER: Called when being read from the save file.
   */
  @Override
  public final void load(CompoundTag tag) {
    super.load(tag);
    readCustomNBT(NBTAction.SAVE, tag);
  }

  /**
   * Called when the chunk data is sent (client receiving chunks from server). Must have x/y/z tags.
   */
  @Override
  public final @Nonnull CompoundTag getUpdateTag() {
    CompoundTag tag = super.getUpdateTag();
    writeCustomNBT(NBTAction.CLIENT, tag);
    if (isProgressTile) {
      // TODO: nicer way to do this? This is needed so players who enter a chunk get a correct progress.
      tag.putFloat("tileprogress", ((IProgressTile) this).getProgress());
    }
    return tag;
  }

  /**
   * CLIENT: Called when chunk data is received (client receiving chunks from server).
   */
  @Override
  public final void handleUpdateTag(@Nonnull CompoundTag tag) {
    super.handleUpdateTag(tag);
    readCustomNBT(NBTAction.CLIENT, tag);
    if (isProgressTile) {
      // TODO: nicer way to do this? This is needed so players who enter a chunk get a correct progress.
      ((IProgressTile) this).setProgress(tag.getFloat("tileprogress"));
    }
  }

  /**
   * SERVER: Called when block data is sent (client receiving blocks from server, via notifyBlockUpdate). No need for x/y/z tags.
   */
  @Override
  public final ClientboundBlockEntityDataPacket getUpdatePacket() {
    CompoundTag tag = new CompoundTag();
    writeCustomNBT(NBTAction.CLIENT, tag);
    if (isProgressTile) {
      // TODO: nicer way to do this? This is needed so players who enter a chunk get a correct progress.
      tag.putFloat("tileprogress", ((IProgressTile) this).getProgress());
    }
    return new ClientboundBlockEntityDataPacket(getBlockPos(), 1, tag);
  }

  /**
   * CLIENT: Called when block data is received (client receiving blocks from server, via notifyBlockUpdate).
   */
  @Override
  public final void onDataPacket(@Nonnull Connection net, @Nonnull ClientboundBlockEntityDataPacket pkt) {
    readCustomNBT(NBTAction.CLIENT, pkt.getTag());
    if (isProgressTile) {
      // TODO: nicer way to do this? This is needed so players who enter a chunk get a correct progress.
      ((IProgressTile) this).setProgress(pkt.getTag().getFloat("tileprogress"));
    }
  }

  protected void writeCustomNBT(@Nonnull ItemStack stack) {
    final CompoundTag tag = new CompoundTag();
    writeCustomNBT(NBTAction.ITEM, tag);
    if (!tag.isEmpty()) {
      stack.setTag(tag);
    }
  }

  @Deprecated
  protected abstract void writeCustomNBT(@Nonnull NBTAction action, @Nonnull CompoundTag root);

  protected void readCustomNBT(@Nonnull ItemStack stack) {
    if (stack.isEmpty()) {
      readCustomNBT(NBTAction.ITEM, NullHelper.notnullM(stack.getTag(), "tag compound vanished"));
    }
  }

  @Deprecated
  protected abstract void readCustomNBT(@Nonnull NBTAction action, @Nonnull CompoundTag root);

  public boolean canPlayerAccess(Player player) {
    BlockPos blockPos = getBlockPos();
    return hasLevel() && !isRemoved() && player.blockPosition().closerThan(getBlockPos(), 64D);
  }

  protected void updateBlock() {
    if (hasLevel() && level.hasChunkAt(getBlockPos())) {
      BlockState bs = level.getBlockState(getBlockPos());
      level.sendBlockUpdated(worldPosition, bs, bs, 3);
    }
  }

  protected boolean isPoweredRedstone() {
    return hasLevel() && level.hasChunkAt(getBlockPos()) ? level.hasNeighborSignal(getBlockPos()) : false;
  }

  /**
   * Called directly after the TE is constructed. This is the place to call non-final methods.
   *
   * Note: This will not be called when the TE is loaded from the save. Hook into the nbt methods for that.
   */
  public void init() {
  }

  /**
   * Call this with an interval (in ticks) to find out if the current tick is the one you want to do some work. This is staggered so the work of different TEs
   * is stretched out over time.
   *
   * @see #shouldDoWorkThisTick(int, int) If you need to offset work ticks
   */
  protected boolean shouldDoWorkThisTick(int interval) {
    return shouldDoWorkThisTick(interval, 0);
  }

  /**
   * Call this with an interval (in ticks) to find out if the current tick is the one you want to do some work. This is staggered so the work of different TEs
   * is stretched out over time.
   *
   * If you have different work items in your TE, use this variant to stagger your work.
   */
  protected boolean shouldDoWorkThisTick(int interval, int offset) {
    return (level.getGameTime() + checkOffset + offset) % interval == 0;
  }

  /**
   * Called server-side when a GhostSlot is changed. Check that the given slot number really is a ghost slot before storing the given stack.
   *
   * @param slot
   *          The slot number that was given to the ghost slot
   * @param stack
   *          The stack that should be placed, null to clear
   * @param realsize
   */
  public void setGhostSlotContents(int slot, @Nonnull ItemStack stack, int realsize) {
  }

  @Override
  public void setChanged() {
    if (hasLevel() && level.hasChunkAt(getBlockPos())) { // we need the loaded check to make sure we don't trigger a chunk load while the chunk is loaded
      level.blockEntityChanged(worldPosition);
      BlockState state = level.getBlockState(worldPosition);
      if (state.hasAnalogOutputSignal()) {
        level.updateNeighbourForOutputSignal(worldPosition, state.getBlock());
      }
    }
  }

  /**
   * Sends an update packet to all players who have this TileEntity loaded. Needed because inventory changes are not synced in a timely manner unless the player
   * has the GUI open. And sometimes the rendering needs the inventory...
   */
  public void forceUpdatePlayers() {
    if (!(level instanceof ServerLevel)) {
      return;
    }

    ServerLevel serverWorld = (ServerLevel) level;
    ClientboundBlockEntityDataPacket updatePacket = getUpdatePacket();
    int chunkX = worldPosition.getX() >> 4;
    int chunkZ = worldPosition.getZ() >> 4;
    for (ServerPlayer serverPlayerEntity : serverWorld.getChunkSource().chunkMap.getPlayers(new ChunkPos(chunkX, chunkZ), false).collect(Collectors.toList())) {
      try {
        serverPlayerEntity.connection.send(updatePacket);
      } catch (Exception e) {
      }
    }
  }
}
