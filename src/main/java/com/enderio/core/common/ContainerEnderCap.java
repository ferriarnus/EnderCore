package com.enderio.core.common;

import java.awt.Point;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.client.gui.widget.GhostSlot;
import com.enderio.core.common.util.NullHelper;
import com.google.common.collect.Maps;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public abstract class ContainerEnderCap<T extends IItemHandler, S extends BlockEntity> extends AbstractContainerMenu implements GhostSlot.IGhostSlotAware {

  protected final @Nonnull Map<Slot, Point> slotLocations = Maps.newLinkedHashMap();

  public Map<Slot, Point> getSlotLocations() {
    return slotLocations;
  }

  protected int startPlayerSlot;
  protected int endPlayerSlot;
  protected int startHotBarSlot;
  protected int endHotBarSlot;

  private final @Nonnull T inv;
  private final @Nonnull Inventory playerInv;
  private final @Nullable S te;

  private boolean initRan = false;

  @Nonnull
  private static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }

  public ContainerEnderCap(@Nullable MenuType<?> type, int id, @Nonnull Inventory playerInv, @Nonnull T itemHandler, @Nullable S te) {
    super(type, id);
    inv = checkNotNull(itemHandler);
    this.playerInv = checkNotNull(playerInv);
    this.te = te;

    init(); // TODO: Drop this line and add the init() call whenever a Container is constructed
  }

  public ContainerEnderCap(@Nullable MenuType<?> type, int id, @Nonnull Inventory playerInv, @Nonnull T itemHandler, @Nullable S te, boolean unused) {
    super(type, id);
    inv = checkNotNull(itemHandler);
    this.playerInv = checkNotNull(playerInv);
    this.te = te;
  }

  // use this if you need to chain it to the new call and care about the exact class
  @SuppressWarnings("unchecked")
  @Nonnull
  public final <X> X init() {
    if (initRan) {
      throw new RuntimeException("Ender IO Internal Error 10T (report this to the Ender IO devs)");
    }
    addSlots();

    int x = getPlayerInventoryOffset().x;
    int y = getPlayerInventoryOffset().y;

    // add players inventory
    startPlayerSlot = slots.size();
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        Slot slot = new Slot(playerInv, j + i * 9 + 9, x + j * 18, y + i * 18);
        addSlot(slot);
      }
    }
    endPlayerSlot = slots.size();

    startHotBarSlot = slots.size();
    for (int i = 0; i < 9; ++i) {
      Slot slot = new Slot(playerInv, i, x + i * 18, y + 58);
      addSlot(slot);
    }
    endHotBarSlot = slots.size();

    initRan = true;
    return (X) this;
  }

  @Override
  protected Slot addSlot(Slot slotIn) {
    slotLocations.put(slotIn, new Point(slotIn.x, slotIn.y));
    return super.addSlot(slotIn);
  }

  @SuppressWarnings("null")
  public @Nonnull List<Slot> getPlayerSlots() {
    return slots.stream().filter(x -> x.container == playerInv).collect(Collectors.toList());
  }

  public @Nonnull Point getPlayerInventoryOffset() {
    return new Point(0, 54);
  }

  public @Nonnull T getItemHandler() {
    return inv;
  }

  public @Nullable S getTileEntity() {
    return te;
  }

  public @Nonnull S getTileEntityNN() {
    return NullHelper.notnull(te, "Internal logic error, TE-less GUI accessing TE");
  }

  @Override
  public boolean stillValid(@Nonnull Player player) {
    if (!initRan) {
      throw new RuntimeException("Ender IO Internal Error 10T (report this to the Ender IO devs)");
    }
    final S te2 = te;
    if (te2 != null) {
      Level world = te2.getLevel();
      BlockPos pos = te2.getBlockPos();
      if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) {
        return false;
      }
      BlockEntity tileEntity = world.getBlockEntity(pos);
      if (te2 != tileEntity) {
        return false;
      }
    }
    return true;
  }

  protected abstract void addSlots();

  @Override
  public void setGhostSlotContents(int slot, @Nonnull ItemStack stack, int realsize) {
    if (te instanceof BlockEntityBase) {
      ((BlockEntityBase) te).setGhostSlotContents(slot, stack, realsize);
    }
  }

  @Override
  public @Nonnull ItemStack quickMoveStack(@Nonnull Player player, int fromSlotId) {
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = slots.get(fromSlotId);

    if (slot != null && slot.hasItem()) {
      ItemStack stackToMove = slot.getItem();
      itemstack = stackToMove.copy();

      if (!mergeItemStack(stackToMove, mapSlotToTargets(fromSlotId))) {
        return ItemStack.EMPTY;
      }

      slot.set(stackToMove); // slot may not be tracking the stack it handed out
      slot.setChanged();
      slot.onTake(player, stackToMove);

      if (player.level.isClientSide && ItemStack.isSame(slot.getItem(), itemstack)) {
        // it seems this slot depends on the server executing the move. Return a different value on client and server to force a sync after the move is
        // executed. And to prevent the client from going into an infinite loop...
        return ItemStack.EMPTY;
      }
    }

    return itemstack;
  }

  /**
   * Creates a mapping for shift-clicks from a slot ID (the on that was shift-clicked) to a list of {@link Slot}s (the ones that can be inserted into).
   * <p>
   * Please note that the "try to fill up stacks" logic will look at <em>all</em> slots before the "move into empty slot" logic runs.
   *
   * @param fromSlotId
   *          The slot that was clicked
   * @return slots the item can go in order of preference
   */
  protected @Nonnull Collection<Slot> mapSlotToTargets(int fromSlotId) {
    List<Slot> result = new ArrayList<>();
    if (fromSlotId < startPlayerSlot) {
      for (int i = startPlayerSlot; i < slots.size(); i++) {
        result.add(0, slots.get(i));
      }
    } else {
      for (int i = 0; i < startPlayerSlot; i++) {
        result.add(slots.get(i));
      }
    }
    return result;
  }

  /**
   * @deprecated unused, see {@link #mergeItemStack(ItemStack, Collection)}
   */
  @Override
  @Deprecated
  protected final boolean moveItemStackTo(@Nonnull ItemStack par1ItemStack, int fromIndex, int toIndex, boolean reversOrder) {
    return false;
  }

  protected boolean mergeItemStack(ItemStack stackToMove, Collection<Slot> targets) {
    boolean result = false;

    if (stackToMove.isStackable()) {
      for (Slot slot : targets) {
        if (isSlotEnabled(slot) && slot.hasItem()) {
          ItemStack stackInSlot = slot.getItem();
          if (stackInSlot.getItem() == stackToMove.getItem()
              && ItemStack.tagMatches(stackToMove, stackInSlot) && slot.mayPlace(stackToMove) && stackToMove != stackInSlot) {
            int mergedSize = stackInSlot.getCount() + stackToMove.getCount();
            int maxStackSize = Math.min(stackToMove.getMaxStackSize(), slot.getMaxStackSize(stackToMove));
            if (mergedSize <= maxStackSize) {
              stackToMove.setCount(0);
              stackInSlot.setCount(mergedSize);
              slot.setChanged();
              return true;
            } else if (stackInSlot.getCount() < maxStackSize) {
              stackToMove.shrink(maxStackSize - stackInSlot.getCount());
              stackInSlot.setCount(maxStackSize);
              slot.setChanged();
              result = true;
            }
          }
        }
      }
    }

    for (Slot slot : targets) {
      if (isSlotEnabled(slot) && !slot.hasItem() && slot.mayPlace(stackToMove)) {
        ItemStack in = stackToMove.copy();
        in.setCount(Math.min(in.getCount(), slot.getMaxStackSize(stackToMove)));
        slot.set(in);
        slot.setChanged();
        stackToMove.shrink(in.getCount());
        if (stackToMove.isEmpty()) {
          return true;
        }
        result = true;
      }
    }

    return result;
  }

  private static final Field listeners;

  static {
    try {
      listeners = ObfuscationReflectionHelper.findField(AbstractContainerMenu.class, "containerListeners");
      listeners.setAccessible(true);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected List<ContainerListener> getListeners() {
    try {
      Object val = listeners.get(this);
      return (List<ContainerListener>) val;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void broadcastChanges() {
    super.broadcastChanges();
    // keep in sync with ContainerEnder#detectAndSendChanges()
    final ClientboundBlockEntityDataPacket updatePacket = te != null ? te.getUpdatePacket() : null;
    if (updatePacket != null) {
      for (ContainerListener containerListener : getListeners()) {
        if (containerListener instanceof ServerPlayer) {
          ((ServerPlayer) containerListener).connection.send(updatePacket);
        }
      }
    }
  }

  protected boolean isSlotEnabled(Slot slot) {
    return slot != null && (!(slot instanceof ContainerEnder.BaseSlot) || slot.isActive())
        && (!(slot instanceof BaseSlotItemHandler) || slot.isActive());
  }

  public static abstract class BaseSlotItemHandler extends SlotItemHandler {

    public BaseSlotItemHandler(@Nonnull IItemHandler itemHandler, int index, int xPosition, int yPosition) {
      super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isActive() {
      // don't super here, super is sided
      return true;
    }

  }

}
