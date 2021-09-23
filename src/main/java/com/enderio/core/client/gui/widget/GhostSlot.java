package com.enderio.core.client.gui.widget;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.client.gui.BaseContainerScreen;
import com.enderio.core.common.BlockEntityBase;
import com.enderio.core.common.network.EnderPacketHandler;
import com.enderio.core.common.network.PacketGhostSlot;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;

public abstract class GhostSlot {

  public static interface IGhostSlotAware {
    /**
     * Called server-side on the container when a GhostSlot is changed. Check that the given slot number really is a ghost slot before storing the given stack.
     *
     * @param slot
     *          The slot number that was given to the ghost slot
     * @param stack
     *          The stack that should be placed, null to clear
     * @param realsize
     *          The real size of the stack if the stack has a size that cannot be stored in an itemstack.
     */
    void setGhostSlotContents(int slot, @Nonnull ItemStack stack, int realsize);
  }

  @Nullable
  private BlockEntityBase te = null;
  private int slot = -1;
  private int x;
  private int y;

  private boolean visible = true;
  private boolean grayOut = true;
  private float grayOutLevel = 0.5f;
  private boolean displayStdOverlay = false;
  private int stackSizeLimit = 1;
  private boolean updateServer = false;
  private boolean drawStdTooltip = true;
  private boolean drawFakeHover = true;

  public boolean isMouseOver(double mx, double my) {
    return mx >= getX() && mx < (getX() + 16) && my >= getY() && my < (getY() + 16);
  }

  public abstract @Nonnull ItemStack getStack();

  public void putStack(@Nonnull ItemStack stack, int realsize) {
    if (shouldUpdateServer()) {
      EnderPacketHandler.sendToServer(PacketGhostSlot.setGhostSlotContents(getSlot(), stack, realsize));
    }
  }

  public int getSlot() {
    return slot;
  }

  public void setSlot(int slot) {
    this.slot = slot;
  }

  /**
   * Should the slot be displayed at all?
   */
  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  /**
   * Should the slot be grayed out?
   */
  public boolean shouldGrayOut() {
    return grayOut;
  }

  public void setGrayOut(boolean grayOut) {
    this.grayOut = grayOut;
  }

  /**
   * If it should be grayed out, how far? (1=no graying, 0=fully invisible)
   */
  public float getGrayOutLevel() {
    return grayOutLevel;
  }

  public void setGrayOutLevel(float grayOutLevel) {
    this.grayOutLevel = grayOutLevel;
  }

  /**
   * Should the items in the slot have their standard overlay (stacksize, damage/energy/fluid bar)?
   */
  public boolean shouldDisplayStdOverlay() {
    return displayStdOverlay;
  }

  public void setDisplayStdOverlay(boolean displayStdOverlay) {
    this.displayStdOverlay = displayStdOverlay;
  }

  /**
   * Limit the stack size? (Enable the standard overlay if this is not 1.)
   */
  public int getStackSizeLimit() {
    return stackSizeLimit;
  }

  public void setStackSizeLimit(int stackSizeLimit) {
    this.stackSizeLimit = stackSizeLimit;
  }

  public boolean shouldDrawStdTooltip() {
    return drawStdTooltip;
  }

  public void setDrawStdTooltip(boolean drawStdTooltip) {
    this.drawStdTooltip = drawStdTooltip;
  }

  public boolean shouldUpdateServer() {
    return updateServer;
  }

  public void setUpdateServer(boolean updateServer) {
    this.updateServer = updateServer;
  }

  public BlockEntityBase getTe() {
    return te;
  }

  public void setTe(BlockEntityBase te) {
    this.te = te;
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public boolean drawGhostSlotTooltip(@Nonnull BaseContainerScreen gui, PoseStack matrixStack, int mouseX, int mouseY) {
    if (drawStdTooltip && gui.getMinecraft().player.getInventory().getSelected().isEmpty()) {
      ItemStack stack = getStack();
      if (!stack.isEmpty()) {
        gui.renderToolTip(matrixStack, stack, mouseX, mouseY);
        return true;
      }
    }
    return false;
  }

  public boolean shouldDrawFakeHover() {
    return drawFakeHover;
  }

  public void setdrawFakeHover(boolean drawFakeHover) {
    this.drawFakeHover = drawFakeHover;
  }

}
