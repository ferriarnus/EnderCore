package com.enderio.core.client.gui;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.client.gui.button.BaseButton;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.ITextComponent;

import com.enderio.core.api.client.gui.IGuiOverlay;
import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.client.gui.TooltipManager.TooltipRenderer;
import com.enderio.core.client.gui.widget.GhostSlot;
import com.enderio.core.client.gui.widget.TooltipWidget;
import com.enderio.core.client.gui.widget.TextFieldEnder;
import com.enderio.core.client.gui.widget.VScrollbar;
import com.enderio.core.common.util.NNList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public abstract class BaseContainerScreen<T extends Container> extends ContainerScreen<T> implements TooltipRenderer, IGuiScreen {

  protected @Nonnull TooltipManager tooltipManager = new TooltipManager();
  protected @Nonnull NNList<IGuiOverlay> overlays = new NNList<>();
  protected @Nonnull NNList<TextFieldEnder> textFields = new NNList<>();
  protected @Nonnull NNList<VScrollbar> scrollbars = new NNList<>();
  protected @Nonnull NNList<IDrawingElement> drawingElements = new NNList<>();
  protected @Nonnull GhostSlotHandler ghostSlotHandler = new GhostSlotHandler();

  protected @Nullable VScrollbar draggingScrollbar;

  private static final Field draggedStackField;

  static {
    draggedStackField = ObfuscationReflectionHelper.findField(ContainerScreen.class, "field_147012_x");
    draggedStackField.setAccessible(true);
  }

  protected BaseContainerScreen(T screenContainer, PlayerInventory inv, ITextComponent titleIn) {
    super(screenContainer, inv, titleIn);
  }

  @Override
  public void init() {
    super.init();
    fixupGuiPosition();
    for (IGuiOverlay overlay : overlays) {
      overlay.init(this);
      children.add(overlay);
    }
    for (TextFieldEnder f : textFields) {
      f.init(this);
    }
  }

  protected void fixupGuiPosition() {
  }

  @Override
  public boolean charTyped(char codePoint, int modifiers) {
    TextFieldEnder focused = null;
    for (TextFieldEnder f : textFields) {
      if (f.isFocused()) {
        focused = f;
      }
    }

    // If esc is pressed
    if (codePoint == 1) {
      // If there is a focused text field unfocus it
      if (focused != null) {
        focused.changeFocus(false);
        focused = null;
        return true; // TODO: Check this is right
      } else if (!hideOverlays()) { // Otherwise close overlays/GUI
        this.minecraft.player.closeScreen();
        return true; // TODO: Check this is right
      }
    }

    // If the user pressed tab, switch to the next text field, or unfocus if there are none
    if (codePoint == '\t') {
      for (int i = 0; i < textFields.size(); i++) {
        TextFieldEnder f = textFields.get(i);
        if (f.isFocused()) {
          textFields.get((i + 1) % textFields.size()).changeFocus(true);
          f.changeFocus(false);
          return true; // TODO: Check this is right
        }
      }
    }

    // If there is a focused text field, attempt to type into it
    if (focused != null) {
      String old = focused.getText();
      if (focused.charTyped(codePoint, modifiers)) {
        onTextFieldChanged(focused, old);
        return true; // TODO: Check this is right
      }
    }

    // More NEI behavior, f key focuses first text field
    if (codePoint == 'f' && focused == null && !textFields.isEmpty()) {
      focused = textFields.get(0);
      focused.changeFocus(true);
    }

    // Finally if 'e' was pressed but not captured by a text field, close the overlays/GUI
    if (codePoint == this.minecraft.gameSettings.keyBindInventory.getKey().getKeyCode()) {
      if (!hideOverlays()) {
        this.minecraft.player.closeScreen();
      }
      return true; // TODO: Check this is right
    }

    // If the key was not captured, let NEI do its thing
    return super.charTyped(codePoint, modifiers);
  }

  protected final void setText(@Nonnull TextFieldEnder tf, @Nonnull String newText) {
    String old = tf.getText();
    tf.setText(newText);
    onTextFieldChanged(tf, old);
  }

  protected void onTextFieldChanged(@Nonnull TextFieldEnder tf, @Nonnull String old) {

  }

  public boolean hideOverlays() {
    for (IGuiOverlay overlay : overlays) {
      if (overlay.isVisible()) {
        overlay.setIsVisible(false);
        return true;
      }
    }
    return false;
  }

  public void addTooltip(@Nonnull TooltipWidget tooltip) {
    tooltipManager.addTooltip(tooltip);
  }

  @Override
  public void tick() {
    super.tick();

    for (TextFieldWidget f : textFields) {
      f.tick();
    }
  }

  @Override
  protected boolean isPointInRegion(int x, int y, int width, int height, double mouseX, double mouseY) {
    double mX = mouseX * this.width / this.minecraft.getMainWindow().getWidth();
    double mY = this.height - mouseY * this.height / this.minecraft.getMainWindow().getHeight() - 1;
    for (IGuiOverlay overlay : overlays) {
      if (overlay.isVisible() && overlay.isMouseInBounds(mX, mY)) {
        return false;
      }
    }
    return super.isPointInRegion(x, y, width, height, mouseX, mouseY);
  }

  @Override
  public @Nonnull GhostSlotHandler getGhostSlotHandler() {
    return ghostSlotHandler;
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    for (TextFieldWidget f : textFields) {
      f.mouseClicked(mouseX, mouseY, button);
    }
    if (!scrollbars.isEmpty()) {
      if (draggingScrollbar != null) {
        draggingScrollbar.mouseClicked(mouseX, mouseY, button);
        return true;
      }
      for (VScrollbar vs : scrollbars) {
        if (vs.mouseClicked(mouseX, mouseY, button)) {
          draggingScrollbar = vs;
          return true;
        }
      }
    }
    if (!getGhostSlotHandler().getGhostSlots().isEmpty()) {
      GhostSlot slot = getGhostSlotHandler().getGhostSlotAt(this, mouseX, mouseY);
      if (slot != null) {
        getGhostSlotHandler().ghostSlotClicked(this, slot, mouseX, mouseY, button);
        return true;
      }
    }
    // Right click field clearing
    if (button == 1) {
      for (TextFieldEnder tf : textFields) {
        if (tf.contains(mouseX, mouseY)) {
          setText(tf, "");
        }
      }
    }
    // Button events for non-left-clicks
    if (button >= 1) {
      for (Object obj : buttons) {
        // TODO: Replace with new buttons system...
        if (obj instanceof BaseButton) {
          BaseButton btn = (BaseButton) obj;
          if (btn.buttonPressed(mouseX, mouseY, button)) {
//            btn.playPressSound(this.mc.getSoundHandler());
            // TODO: What is this actionPerformed thing
//            actionPerformedButton(btn, button);
            return true;
          }
        }
      }
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public void drawHoveringTooltipText(MatrixStack matrixStack, @Nonnull List<ITextComponent> tooltip, int mouseX, int mouseY, @Nonnull FontRenderer font) {
    renderWrappedToolTip(matrixStack, tooltip, mouseX, mouseY, font);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (draggingScrollbar != null) {
      draggingScrollbar.mouseReleased(mouseX, mouseY, button);
      draggingScrollbar = null;
    }
    return super.mouseReleased(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
    if (draggingScrollbar != null) {
      return draggingScrollbar.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    if (this.getListener() != null && this.isDragging()
        && button == 0 && this.getListener().mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
      return true;
    }
    return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    if (!scrollbars.isEmpty()) {
      for (VScrollbar vs : scrollbars) {
        vs.mouseScrolled(mouseX, mouseY, delta);
      }
    }
    if (!getGhostSlotHandler().getGhostSlots().isEmpty()) {
      GhostSlot slot = getGhostSlotHandler().getGhostSlotAt(this, mouseX, mouseY);
      if (slot != null) {
        getGhostSlotHandler().ghostSlotClicked(this, slot, mouseX, mouseY, delta < 0 ? -1 : -2);
      }
    }

    return super.mouseScrolled(mouseX, mouseY, delta);
  }

  public void addOverlay(@Nonnull IGuiOverlay overlay) {
    overlays.add(overlay);
  }

  public void removeOverlay(@Nonnull IGuiOverlay overlay) {
    overlays.remove(overlay);
  }

  public void addScrollbar(@Nonnull VScrollbar vs) {
    scrollbars.add(vs);
    vs.adjustPosition();
  }

  public void removeScrollbar(@Nonnull VScrollbar vs) {
    scrollbars.remove(vs);
    if (draggingScrollbar == vs) {
      draggingScrollbar = null;
    }
  }

  public void addDrawingElement(@Nonnull IDrawingElement element) {
    drawingElements.add(element);
    TooltipWidget tooltip = element.getTooltip();
    if (tooltip != null) {
      addTooltip(tooltip);
    }
  }

  public void removeDrawingElement(@Nonnull IDrawingElement element) {
    drawingElements.remove(element);
    TooltipWidget tooltip = element.getTooltip();
    if (tooltip != null) {
      removeTooltip(tooltip);
    }
  }

  private int realMx, realMy;

  @Override
  protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
    drawForegroundImpl(matrixStack, x, y);

    matrixStack.push();
    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    RenderSystem.disableDepthTest();
    setBlitOffset(300);
    itemRenderer.zLevel = 300.0F;
    for (IGuiOverlay overlay : overlays) {
      if (overlay.isVisible()) {
        overlay.draw(realMx, realMy, minecraft.getRenderPartialTicks());
      }
    }
    setBlitOffset(0);
    itemRenderer.zLevel = 0F;
    RenderSystem.enableDepthTest();
    matrixStack.pop();
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
    for (IDrawingElement drawingElement : drawingElements) {
      drawingElement.drawGuiContainerBackgroundLayer(partialTicks, x, y);
    }
    for (TextFieldWidget f : textFields) {
      f.render(matrixStack, x, y, partialTicks);
    }
    if (!scrollbars.isEmpty()) {
      for (VScrollbar vs : scrollbars) {
        vs.drawScrollbar(matrixStack, x, y);
      }
    }
    if (!ghostSlotHandler.getGhostSlots().isEmpty()) {
      getGhostSlotHandler().drawGhostSlots(this, matrixStack, x, y);
    }
  }

  @Override
  public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    realMx = mouseX;
    realMy = mouseY;
    this.renderBackground(matrixStack);
    super.render(matrixStack, mouseX, mouseY, partialTicks);

    // try to only draw one tooltip...
    if (draggingScrollbar == null) {
      if (!renderHoveredToolTip2(matrixStack, mouseX, mouseY)) {
        if (!ghostSlotHandler.drawGhostSlotTooltip(this, matrixStack, mouseX, mouseY)) {
          tooltipManager.drawTooltips(matrixStack, this, mouseX, mouseY);
        }
      }
    }
  }

  /**
   * See {@link #renderHoveredTooltip(MatrixStack, int, int)} but with a feedback return value
   */
  protected boolean renderHoveredToolTip2(MatrixStack matrixStack, int x, int y) {
    if (minecraft.player.inventory.getItemStack().isEmpty()) {
      final Slot slotUnderMouse = getSlotUnderMouse();
      if (slotUnderMouse != null && slotUnderMouse.getHasStack()) {
        this.renderTooltip(matrixStack, this.hoveredSlot.getStack(), x, y);
        return true;
      }
    }
    return false;
  }

  public void renderToolTip(MatrixStack matrixStack, ItemStack stack, int x, int y) {
    this.renderTooltip(matrixStack, stack, x, y);
  }


  protected void drawItemStack(ItemStack stack, int x, int y, String altText) {
    RenderSystem.translatef(0.0F, 0.0F, 32.0F);
    this.setBlitOffset(200);
    this.itemRenderer.zLevel = 200.0F;
    net.minecraft.client.gui.FontRenderer font = stack.getItem().getFontRenderer(stack);
    if (font == null) font = this.font;
    this.itemRenderer.renderItemAndEffectIntoGUI(stack, x, y);
    this.itemRenderer.renderItemOverlayIntoGUI(font, stack, x, y - (getDraggedStack().isEmpty() ? 0 : 8), altText);
    this.setBlitOffset(0);
    this.itemRenderer.zLevel = 0.0F;
  }

  protected ItemStack getDraggedStack() {
    try {
      return (ItemStack) draggedStackField.get(this);
    } catch (IllegalAccessException e) {
      //Is never called
      e.printStackTrace();
      return ItemStack.EMPTY;
    }
  }


  protected void drawFakeItemsStart() {
    setBlitOffset(100);
    itemRenderer.zLevel = 100.0F;

    RenderSystem.enableLighting();
    RenderSystem.enableRescaleNormal();
    RenderSystem.enableDepthTest();
    RenderHelper.enableStandardItemLighting();
  }

  public void drawFakeItemStack(int x, int y, @Nonnull ItemStack stack) {
    itemRenderer.renderItemAndEffectIntoGUI(stack, x, y);
    RenderSystem.enableAlphaTest();
  }

  public void drawFakeItemStackStdOverlay(int x, int y, @Nonnull ItemStack stack) {
    itemRenderer.renderItemOverlayIntoGUI(font, stack, x, y, null);
  }

  protected void drawFakeItemHover(MatrixStack matrixStack, int x, int y) {
    RenderSystem.disableLighting();
    RenderSystem.disableDepthTest();
    RenderSystem.colorMask(true, true, true, false);
    blit(matrixStack, x, y, x + 16, y + 16, 0x80FFFFFF, 0x80FFFFFF);
    RenderSystem.colorMask(true, true, true, true);
    RenderSystem.enableDepthTest();

    RenderSystem.enableLighting();
  }

  protected void drawFakeItemsEnd() {
    itemRenderer.zLevel = 0.0F;
    setBlitOffset(0);
  }

  /**
   * Return the current texture to allow GhostSlots to gray out by over-painting the slot background.
   */
  protected abstract @Nonnull ResourceLocation getGuiTexture();

  @Override
  public boolean removeTooltip(@Nonnull TooltipWidget tooltip) {
    return tooltipManager.removeTooltip(tooltip);
  }

  protected void drawForegroundImpl(MatrixStack matrixStack, int mouseX, int mouseY) {
    super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
  }

  @Override
  public int getGuiLeft() {
    return guiLeft;
  }

  @Override
  public int getGuiTop() {
    return guiTop;
  }

  @Override
  public int getXSize() {
    return xSize;
  }

  @Override
  public int getYSize() {
    return ySize;
  }

  public void setGuiLeft(int i) {
    guiLeft = i;
  }

  public void setGuiTop(int i) {
    guiTop = i;
  }

  public void setXSize(int i) {
    xSize = i;
  }

  public void setYSize(int i) {
    ySize = i;
  }

  @Override
  public @Nonnull FontRenderer getFontRenderer() {
    return Minecraft.getInstance().fontRenderer;
  }

  public void removeButton(@Nonnull Button button) {
    buttons.remove(button);
    children.remove(button);
  }

  @Override
  public int getOverlayOffsetXLeft() {
    return 0;
  }

  @Override
  public int getOverlayOffsetXRight() {
    return 0;
  }

  @Override
  public void doActionPerformed(@Nonnull Button button) throws IOException {

  }

  @Override
  public void clearTooltips() {
    tooltipManager.clearToolTips();
  }

  @Override
  public void onClose() {
    super.onClose();
    for (IGuiOverlay overlay : overlays) {
      overlay.onClose();
    }
  }

  @Override
  public final int getGuiRootLeft() {
    return getGuiLeft();
  }

  @Override
  public final int getGuiRootTop() {
    return getGuiTop();
  }

  @Override
  public final int getGuiXSize() {
    return getXSize();
  }

  @Override
  public final int getGuiYSize() {
    return getYSize();
  }

  @Override
  @Nonnull
  public final <T extends Button> T addGuiButton(@Nonnull T button) {
    return addButton(button);
  }

}
