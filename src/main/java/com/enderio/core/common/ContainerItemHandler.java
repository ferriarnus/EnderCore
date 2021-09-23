package com.enderio.core.common;

import java.awt.Point;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.client.gui.widget.GhostSlot;
import com.google.common.collect.Maps;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

@Deprecated // not used by anyone, so probably should be used without good reason?
public abstract class ContainerItemHandler<T extends ICapabilityProvider> extends AbstractContainerMenu implements GhostSlot.IGhostSlotAware {

  protected final @Nonnull Map<Slot, Point> playerSlotLocations = Maps.newLinkedHashMap();

  protected final int startPlayerSlot;
  protected final int endPlayerSlot;
  protected final int startHotBarSlot;
  protected final int endHotBarSlot;

  private final @Nonnull T owner;
  private final @Nonnull IItemHandler inv;
  private final @Nonnull Inventory playerInv;

  @Nonnull
  private static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }

  public ContainerItemHandler(@Nullable MenuType<?> type, int id, @Nonnull Inventory playerInv, @Nonnull T owner, @Nullable Direction direction) {
    super(type, id);
    this.owner = checkNotNull(owner);
    this.inv = checkNotNull(owner.getCapability(checkNotNull(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY), direction).resolve().orElse(null));
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

  public @Nonnull T getOwner() {
    return owner;
  }

  public @Nonnull IItemHandler getInv() {
    return inv;
  }

  @Override
  public @Nonnull ItemStack quickMoveStack(@Nonnull Player p_82846_1_, int p_82846_2_) {
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.slots.get(p_82846_2_);

    if (slot != null && slot.hasItem()) {
      ItemStack itemstack1 = slot.getItem();
      itemstack = itemstack1.copy();

      int minPlayerSlot = slots.size() - playerInv.items.size();
      if (p_82846_2_ < minPlayerSlot) {
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
    }

    return itemstack;
  }

  /**
   * Added validation of slot input
   */
  @Override
  protected boolean moveItemStackTo(@Nonnull ItemStack par1ItemStack, int fromIndex, int toIndex, boolean reversOrder) {

    boolean result = false;
    int checkIndex = fromIndex;

    if (reversOrder) {
      checkIndex = toIndex - 1;
    }

    Slot slot;
    ItemStack itemstack1;

    if (par1ItemStack.isStackable()) {

      while (!par1ItemStack.isEmpty() && (!reversOrder && checkIndex < toIndex || reversOrder && checkIndex >= fromIndex)) {
        slot = this.slots.get(checkIndex);
        itemstack1 = slot.getItem();

        if (!itemstack1.isEmpty() && itemstack1.getItem() == par1ItemStack.getItem()
            && ItemStack.tagMatches(par1ItemStack, itemstack1) && slot.mayPlace(par1ItemStack) && par1ItemStack != itemstack1) {

          int mergedSize = itemstack1.getCount() + par1ItemStack.getCount();
          int maxStackSize = Math.min(par1ItemStack.getMaxStackSize(), slot.getMaxStackSize());
          if (mergedSize <= maxStackSize) {
            par1ItemStack.setCount(0);
            itemstack1.setCount(mergedSize);
            slot.setChanged();
            result = true;
          } else if (itemstack1.getCount() < maxStackSize) {
            par1ItemStack.shrink(maxStackSize - itemstack1.getCount());
            itemstack1.setCount(maxStackSize);
            slot.setChanged();
            result = true;
          }
        }

        if (reversOrder) {
          --checkIndex;
        } else {
          ++checkIndex;
        }
      }
    }

    if (!par1ItemStack.isEmpty()) {
      if (reversOrder) {
        checkIndex = toIndex - 1;
      } else {
        checkIndex = fromIndex;
      }

      while (!reversOrder && checkIndex < toIndex || reversOrder && checkIndex >= fromIndex) {
        slot = this.slots.get(checkIndex);
        itemstack1 = slot.getItem();

        if (itemstack1.isEmpty() && slot.mayPlace(par1ItemStack)) {
          ItemStack in = par1ItemStack.copy();
          in.setCount(Math.min(in.getCount(), slot.getMaxStackSize()));

          slot.set(in);
          slot.setChanged();
          par1ItemStack.shrink(in.getCount());
          result = true;
          break;
        }

        if (reversOrder) {
          --checkIndex;
        } else {
          ++checkIndex;
        }
      }
    }

    return result;
  }

  @Override
  public void setGhostSlotContents(int slot, @Nonnull ItemStack stack, int realsize) {
    if (owner instanceof BlockEntityBase) {
      ((BlockEntityBase) owner).setGhostSlotContents(slot, stack, realsize);
    }

  }
}
