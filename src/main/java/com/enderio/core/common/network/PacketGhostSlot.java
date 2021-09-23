package com.enderio.core.common.network;

import javax.annotation.Nonnull;

import com.enderio.core.client.gui.widget.GhostSlot;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketGhostSlot {

  int windowId;
  int slot;
  @Nonnull
  ItemStack stack = ItemStack.EMPTY;
  int realsize;

  public PacketGhostSlot() {
  }

  public PacketGhostSlot(FriendlyByteBuf buffer) {
    windowId = buffer.readInt();
    slot = buffer.readShort();
    stack = buffer.readItem();
    realsize = buffer.readInt();
  }

  public static PacketGhostSlot setGhostSlotContents(int slot, @Nonnull ItemStack stack, int realsize) {
    PacketGhostSlot msg = new PacketGhostSlot();
    msg.slot = slot;
    msg.stack = stack;
    msg.realsize = realsize;
    msg.windowId = Minecraft.getInstance().player.containerMenu.containerId;
    return msg;
  }

  public void toBytes(FriendlyByteBuf buffer) {
    buffer.writeInt(windowId);
    buffer.writeShort(slot);
    buffer.writeItem(stack);
    buffer.writeInt(realsize);
  }

  public boolean handle(Supplier<NetworkEvent.Context> context) {
    AbstractContainerMenu openContainer = context.get().getSender().containerMenu;
    if (openContainer instanceof GhostSlot.IGhostSlotAware && openContainer.containerId == windowId) {
      ((GhostSlot.IGhostSlotAware) openContainer).setGhostSlotContents(slot, stack, realsize);
    }
    return false;
  }
}
