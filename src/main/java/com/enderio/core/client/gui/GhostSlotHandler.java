package com.enderio.core.client.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.client.gui.widget.GhostSlot;
import com.enderio.core.client.render.RenderUtil;
import com.enderio.core.common.util.ItemUtil;
import com.enderio.core.common.util.NNList;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public class GhostSlotHandler {

  protected final @Nonnull NNList<GhostSlot> ghostSlots = new NNList<>();
  protected @Nullable GhostSlot hoverGhostSlot;

  @Nonnull
  public NNList<GhostSlot> getGhostSlots() {
    return ghostSlots;
  }

  public void add(GhostSlot slot) {
    ghostSlots.add(slot);
  }

  public GhostSlot getGhostSlotAt(@Nonnull BaseContainerScreen baseContainerScreen, double mouseX, double mouseY) {
    double mX = mouseX - baseContainerScreen.getGuiLeft();
    double mY = mouseY - baseContainerScreen.getGuiTop();
    for (GhostSlot slot : ghostSlots) {
      if (slot.isVisible() && slot.isMouseOver(mX, mY) && slot.shouldDrawFakeHover()) {
        return slot;
      }
    }
    return null;
  }

  // Slot interaction

  /**
   * Called when a ghost slot is clicked or mouse wheeled.
   *
   * @param gui
   *          The GUI the GhostSlot is in
   * @param slot
   *          The GhostSlot
   * @param x
   *          Mouse position x
   * @param y
   *          Mouse position y
   * @param button
   *          The button used (0=left, 1=right). The mouse wheel is mapped to -1=down and -2=up.
   */
  protected void ghostSlotClicked(@Nonnull BaseContainerScreen gui, @Nonnull GhostSlot slot, double x, double y, int button) {
    ItemStack handStack = Minecraft.getInstance().player.getInventory().getSelected();
    ItemStack existingStack = slot.getStack();
    if (button == 0) { // left
      ghostSlotClickedPrimaryMouseButton(slot, handStack, existingStack);
    } else if (button == 1) { // right
      ghostSlotClickedSecondaryMouseButton(slot, handStack, existingStack);
    } else if (button == -2) { // wheel up
      ghostSlotClickedMouseWheelUp(slot, handStack, existingStack);
    } else if (button == -1) { // wheel down
      ghostSlotClickedMouseWheelDown(slot, handStack, existingStack);
    }
  }

  protected void ghostSlotClickedPrimaryMouseButton(@Nonnull GhostSlot slot, @Nonnull ItemStack handStack, @Nonnull ItemStack existingStack) {
    if (handStack.isEmpty()) { // empty hand
      slot.putStack(ItemStack.EMPTY, 0);
    } else { // item in hand
      if (existingStack.isEmpty()) { // empty slot
        replaceSlot(slot, handStack);
      } else { // filled slot
        if (ItemUtil.areStackMergable(existingStack, handStack)) { // same item
          if (existingStack.getCount() < existingStack.getMaxStackSize() && existingStack.getCount() < slot.getStackSizeLimit()) {
            increaseSlot(slot, existingStack);
          } else {
            // NOP
          }
        } else { // different item
          replaceSlot(slot, handStack);
        }
      }
    }
  }

  protected void ghostSlotClickedSecondaryMouseButton(@Nonnull GhostSlot slot, @Nonnull ItemStack handStack, @Nonnull ItemStack existingStack) {
    if (handStack.isEmpty()) { // empty hand
      slot.putStack(ItemStack.EMPTY, 0);
    } else { // item in hand
      if (existingStack.isEmpty()) { // empty slot
        replaceSlot1Item(slot, handStack);
      } else { // filled slot
        if (ItemUtil.areStackMergable(existingStack, handStack)) { // same item
          descreaseSlot(slot, existingStack);
        } else { // different item
          replaceSlot1Item(slot, handStack);
        }
      }
    }
  }

  protected void ghostSlotClickedMouseWheelUp(@Nonnull GhostSlot slot, @Nonnull ItemStack handStack, @Nonnull ItemStack existingStack) {
    if (!existingStack.isEmpty() && existingStack.getCount() < existingStack.getMaxStackSize() && existingStack.getCount() < slot.getStackSizeLimit()) {
      increaseSlot(slot, existingStack);
    }
  }

  protected void ghostSlotClickedMouseWheelDown(@Nonnull GhostSlot slot, @Nonnull ItemStack handStack, @Nonnull ItemStack existingStack) {
    if (!existingStack.isEmpty()) {
      descreaseSlot(slot, existingStack);
    }
  }

  // Slot interaction tools

  protected void descreaseSlot(@Nonnull GhostSlot slot, @Nonnull ItemStack existingStack) {
    existingStack.shrink(1);
    slot.putStack(existingStack, existingStack.getCount());
  }

  protected void increaseSlot(@Nonnull GhostSlot slot, @Nonnull ItemStack existingStack) {
    existingStack.grow(1);
    slot.putStack(existingStack, existingStack.getCount());
  }

  protected void replaceSlot1Item(@Nonnull GhostSlot slot, @Nonnull ItemStack handStack) {
    ItemStack oneItem = handStack.copy();
    oneItem.setCount(1);
    slot.putStack(oneItem, oneItem.getCount());
  }

  protected void replaceSlot(@Nonnull GhostSlot slot, @Nonnull ItemStack handStack) {
    if (handStack.getCount() <= slot.getStackSizeLimit()) {
      slot.putStack(handStack, handStack.getCount());
    } else {
      ItemStack tmp = handStack.copy();
      tmp.setCount(slot.getStackSizeLimit());
      slot.putStack(tmp, tmp.getCount());
    }
  }

  // Rendering

  protected void startDrawing(@Nonnull BaseContainerScreen gui) {
    hoverGhostSlot = null;
  }

  protected void drawGhostSlots(@Nonnull BaseContainerScreen gui, PoseStack matrixStack, int mouseX, int mouseY) {
    int sx = gui.getGuiLeft();
    int sy = gui.getGuiTop();
    gui.drawFakeItemsStart();
    try {
      hoverGhostSlot = null;
      for (GhostSlot slot : ghostSlots) {
        ItemStack stack = slot.getStack();
        if (slot.isVisible()) {
          if (!stack.isEmpty()) {
            gui.drawFakeItemStack(slot.getX() + sx, slot.getY() + sy, stack);
            if (slot.shouldDisplayStdOverlay()) {
              gui.drawFakeItemStackStdOverlay(slot.getX() + sx, slot.getY() + sy, stack);
            }
            if (slot.shouldGrayOut()) {
              drawGhostSlotGrayout(gui, matrixStack, slot);
            }
          }
          if (slot.isMouseOver(mouseX - sx, mouseY - sy)) {
            hoverGhostSlot = slot;
          }
        }
      }
      final GhostSlot hoverGhostSlot2 = hoverGhostSlot;
      if (hoverGhostSlot2 != null && hoverGhostSlot2.shouldDrawFakeHover()) {
        // draw hover last to prevent it from affecting rendering of other slots ...
        gui.drawFakeItemHover(matrixStack, hoverGhostSlot2.getX() + sx, hoverGhostSlot2.getY() + sy);
      }
    } finally {
      gui.drawFakeItemsEnd();
    }
  }

  /**
   * Gray out the item that was just painted into a GhostSlot by over-painting it with 50% transparent background. This gives the illusion that the item was
   * painted with 50% transparency. (100%*a ° 100%*b ° 50%*a == 100%*a ° 50%*b)
   */
  protected void drawGhostSlotGrayout(@Nonnull BaseContainerScreen gui, PoseStack matrixStack, @Nonnull GhostSlot slot) {
    RenderSystem.disableDepthTest();
    RenderSystem.enableBlend();
    //RenderSystem.disableLighting();
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, slot.getGrayOutLevel());
    RenderUtil.bindTexture(gui.getGuiTexture());
    gui.blit(matrixStack, gui.getGuiLeft() + slot.getX(), gui.getGuiTop() + slot.getY(), slot.getX(), slot.getY(), 16, 16);
    RenderSystem.disableBlend();
    RenderSystem.enableDepthTest();
  }

  protected boolean drawGhostSlotTooltip(@Nonnull BaseContainerScreen gui, PoseStack matrixStack, int mouseX, int mouseY) {
    final GhostSlot hoverGhostSlot2 = hoverGhostSlot;
    if (hoverGhostSlot2 != null) {
      return hoverGhostSlot2.drawGhostSlotTooltip(gui, matrixStack, mouseX, mouseY);
    }
    return false;
  }

}
