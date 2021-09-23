package com.enderio.core.common;

import java.awt.Point;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.client.gui.widget.GhostSlot;
import com.enderio.core.common.ContainerEnderCap.BaseSlotItemHandler;
import com.google.common.collect.Maps;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

@Deprecated
public class ContainerEnder<T extends Container> extends AbstractContainerMenu implements GhostSlot.IGhostSlotAware {

  protected final @Nonnull Map<Slot, Point> playerSlotLocations = Maps.newLinkedHashMap();

  protected final int startPlayerSlot;
  protected final int endPlayerSlot;
  protected final int startHotBarSlot;
  protected final int endHotBarSlot;

  private final @Nonnull T inv;
  private final @Nonnull Inventory playerInv;

  @Nonnull
  private static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }

  public ContainerEnder(@Nullable MenuType<?> type, int id, @Nonnull Inventory playerInv, @Nonnull T inv) {
    super(type, id);
    this.inv = checkNotNull(inv);
    this.playerInv = checkNotNull(playerInv);

    addSlots(this.playerInv);

    int x = getPlayerInventoryOffset().x;
    int y = getPlayerInventoryOffset().y;

    // add players inventory
    startPlayerSlot = slots.size();
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        Point loc = new Point(x + j * 18, y + i * 18);
        Slot slot = new Slot(this.playerInv, j + i * 9 + 9, loc.x, loc.y);
        addSlot(slot);
        playerSlotLocations.put(slot, loc);
      }
    }
    endPlayerSlot = slots.size();

    startHotBarSlot = slots.size();
    for (int i = 0; i < 9; ++i) {
      Point loc = new Point(x + i * 18, y + 58);
      Slot slot = new Slot(this.playerInv, i, loc.x, loc.y);
      addSlot(slot);
      playerSlotLocations.put(slot, loc);
    }
    endHotBarSlot = slots.size();
  }

  protected void addSlots(@Nonnull Inventory playerInventory) {
  }

  public @Nonnull Point getPlayerInventoryOffset() {
    return new Point(8, 84);
  }

  public @Nonnull Point getUpgradeOffset() {
    return new Point(12, 60);
  }

  public @Nonnull T getInv() {
    return inv;
  }


  @Nonnull
  public Slot getSlotFromInventory(int slotIn) {
    return slots.get(slotIn);
  }


  @Override
  public boolean stillValid(Player playerIn) {
    return getInv().stillValid(playerIn);
  }

  @Override
  public ItemStack quickMoveStack(Player playerIn, int index) {
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.slots.get(index);

    if (slot != null && slot.hasItem()) {
      ItemStack itemstack1 = slot.getItem();
      itemstack = itemstack1.copy();

      int minPlayerSlot = slots.size() - playerInv.items.size();
      if (index < minPlayerSlot) {
        if (!this.moveItemStackTo(itemstack1, minPlayerSlot, this.slots.size(), true)) {
          return ItemStack.EMPTY;
        }
      } else if (!this.moveItemStackTo(itemstack1, 0, minPlayerSlot, false)) {
        return ItemStack.EMPTY;
      }

      if (itemstack1.isEmpty()) {
        slot.set(ItemStack.EMPTY);
      } else {
        slot.setChanged();
      }

      if (itemstack1.getCount() == itemstack.getCount()) {
        return ItemStack.EMPTY;
      }

      slot.onTake(playerIn, itemstack1);
    }

    return itemstack;
  }

  /**
   * Added validation of slot input
   */
  @Override
  protected boolean moveItemStackTo(@Nonnull ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
    boolean flag = false;
    int i = startIndex;
    if (reverseDirection) {
      i = endIndex - 1;
    }

    if (stack.isStackable()) {
      while(!stack.isEmpty()) {
        if (reverseDirection) {
          if (i < startIndex) {
            break;
          }
        } else if (i >= endIndex) {
          break;
        }

        Slot slot = this.slots.get(i);
        ItemStack itemstack = slot.getItem();
        if (!itemstack.isEmpty() && ItemStack.isSameItemSameTags(stack, itemstack)) {
          int j = itemstack.getCount() + stack.getCount();
          int maxSize = Math.min(slot.getMaxStackSize(), stack.getMaxStackSize());
          if (j <= maxSize) {
            stack.setCount(0);
            itemstack.setCount(j);
            slot.setChanged();
            flag = true;
          } else if (itemstack.getCount() < maxSize) {
            stack.shrink(maxSize - itemstack.getCount());
            itemstack.setCount(maxSize);
            slot.setChanged();
            flag = true;
          }
        }

        if (reverseDirection) {
          --i;
        } else {
          ++i;
        }
      }
    }

    if (!stack.isEmpty()) {
      if (reverseDirection) {
        i = endIndex - 1;
      } else {
        i = startIndex;
      }

      while(true) {
        if (reverseDirection) {
          if (i < startIndex) {
            break;
          }
        } else if (i >= endIndex) {
          break;
        }

        Slot slot1 = this.slots.get(i);
        ItemStack itemstack1 = slot1.getItem();
        if (itemstack1.isEmpty() && slot1.mayPlace(stack)) {
          if (stack.getCount() > slot1.getMaxStackSize()) {
            slot1.set(stack.split(slot1.getMaxStackSize()));
          } else {
            slot1.set(stack.split(stack.getCount()));
          }

          slot1.setChanged();
          flag = true;
          break;
        }

        if (reverseDirection) {
          --i;
        } else {
          ++i;
        }
      }
    }

    return flag;
  }

  @Override
  public void setGhostSlotContents(int slot, @Nonnull ItemStack stack, int realsize) {
    if (inv instanceof BlockEntityBase) {
      ((BlockEntityBase) inv).setGhostSlotContents(slot, stack, realsize);
    }
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
    if (inv instanceof BlockEntityBase) {
      // keep in sync with ContainerEnderCap#detectAndSendChanges()
      final ClientboundBlockEntityDataPacket updatePacket = ((BlockEntityBase) inv).getUpdatePacket();
      if (updatePacket != null) {
        for (ContainerListener containerListener : getListeners()) {
          if (containerListener instanceof ServerPlayer) {
            ((ServerPlayer) containerListener).connection.send(updatePacket);
          }
        }
      }
    }
  }

  private boolean isSlotEnabled(Slot slot) {
    return slot != null && (!(slot instanceof ContainerEnder.BaseSlot) || slot.isActive())
        && (!(slot instanceof BaseSlotItemHandler) || slot.isActive());
  }

  public static abstract class BaseSlot extends Slot {

    public BaseSlot(@Nonnull Container inventoryIn, int index, int xPosition, int yPosition) {
      super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isActive() {
      // don't super here, super is sided
      return true;
    }

  }

}
