package com.enderio.core.client.gui.widget;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.api.client.gui.ListSelectionListener;
import com.enderio.core.client.render.ColorUtil;
import com.enderio.core.common.vecmath.Vec3f;
import com.enderio.core.common.vecmath.Vec4f;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.GameRenderer;

public abstract class ScrollableListWidget<T> implements GuiEventListener{

  private final @Nonnull Minecraft mc = Minecraft.getInstance();

  protected int originX;
  protected int originY;
  protected int width;
  protected int height;
  protected int minY;
  protected int maxY;
  protected int minX;
  protected int maxX;

  protected final int slotHeight;

  private float initialClickY = -2.0F;

  private float scrollMultiplier;

  private float amountScrolled;

  protected int selectedIndex = -1;

  private long lastClickedTime;

  private boolean showSelectionBox = true;

  protected int margin = 2;

  protected @Nonnull List<ListSelectionListener<T>> listeners = new CopyOnWriteArrayList<ListSelectionListener<T>>();

  public ScrollableListWidget(int width, int height, int originX, int originY, int slotHeight) {
    this.width = width;
    this.height = height;
    this.originX = originX;
    this.originY = originY;
    this.slotHeight = slotHeight;

    minY = originY;
    maxY = minY + height;
    minX = originX;
    maxX = minX + width;
  }

  public void onGuiInit(@Nonnull IGuiScreen gui) {
    minY = originY + gui.getGuiRootTop();
    maxY = minY + height;
    minX = originX + gui.getGuiRootLeft();
    maxX = minX + width;
  }

  public void addSelectionListener(@Nonnull ListSelectionListener<T> listener) {
    listeners.add(listener);
  }

  public void removeSelectionListener(@Nonnull ListSelectionListener<T> listener) {
    listeners.remove(listener);
  }

  public @Nullable T getSelectedElement() {
    return selectedIndex < 0 || selectedIndex >= getNumElements() ? null : getElementAt(selectedIndex);
  }

  public void setSelection(@Nonnull T selection) {
    setSelection(getIndexOf(selection));
  }

  public void setSelection(int index) {
    if (index == selectedIndex) {
      return;
    }
    selectedIndex = index;
    for (ListSelectionListener<T> listener : listeners) {
      listener.selectionChanged(this, selectedIndex);
    }
  }

  public int getIndexOf(@Nullable T element) {
    if (element == null) {
      return -1;
    }
    for (int i = 0; i < getNumElements(); i++) {
      if (element.equals(getElementAt(i))) {
        return i;
      }
    }
    return -1;
  }

  public abstract @Nonnull T getElementAt(int index);

  public abstract int getNumElements();

  protected abstract void drawElement(int elementIndex, int x, int y, int h, @Nonnull BufferBuilder renderer);

  protected boolean elementClicked(int elementIndex, boolean doubleClick, int elementX, int elementY) {
    return true;
  }

  public void setShowSelectionBox(boolean val) {
    showSelectionBox = val;
  }

  protected int getContentHeight() {
    return getNumElements() * slotHeight;
  }

  private void clampScrollToBounds() {
    int i = getContentOverhang();
    if (i < 0) {
      i *= -1;
    }
    if (amountScrolled < 0.0F) {
      amountScrolled = 0.0F;
    }
    if (amountScrolled > i) {
      amountScrolled = i;
    }
  }

  public int getContentOverhang() {
    return getContentHeight() - (height - margin);
  }

  //OnPress to be used in the "up" button
  public OnPress scrollUp() {
    return new OnPress() {
      
      @Override
      public void onPress(Button button) {
        amountScrolled -= slotHeight * 2 / 3;
        initialClickY = -2.0F;
        clampScrollToBounds();
      }
    };
  }
  
  //OnPress to be used in the "down" button
  public OnPress scrollDown() {
    return new OnPress() {

      @Override
      public void onPress(Button button) {
        amountScrolled += slotHeight * 2 / 3;
      initialClickY = -2.0F;
      clampScrollToBounds();
      }
    };
  }

  public T getElementAt(int mX, int mY) {
    if (mY >= minY && mY <= maxY && mX >= minX && mX <= maxX + 6) {
      int y = mY - minY + (int) amountScrolled - margin;
      int mouseOverElement = y / slotHeight;
      if (mX >= minX && mX <= maxX && mouseOverElement >= 0 && y >= 0 && mouseOverElement < getNumElements()) {
        return getElementAt(mouseOverElement);
      }
    }
    return null;
  }

  /**
   * draws the slot to the screen, pass in mouse's current x and y and partial ticks
   */
  public void drawScreen(int mX, int mY, float partialTick) {

    clampScrollToBounds();
    
    //RenderSystem.disableLighting();
    RenderSystem.getShaderFogEnd();

    final @Nonnull Window mw = mc.getWindow();
    final int sx = (int) (minX * mw.getGuiScale());
    final int sw = (int) (width * mw.getGuiScale());
    final int sy = (int) (mw.getGuiScaledHeight() - maxY * mw.getGuiScale());
    final int sh = (int) (height * mw.getGuiScale());
    GL11.glEnable(GL11.GL_SCISSOR_TEST);
    GL11.glScissor(sx, sy, sw, sh);

    final @Nonnull BufferBuilder renderer = Tesselator.getInstance().getBuilder();
    drawContainerBackground(renderer);

    final int contentYOffset = this.minY + margin - (int) this.amountScrolled;
    final int drawHeight = this.slotHeight - margin;

    final Vec4f col = ColorUtil.toFloat4(8421504);
    final Vec4f colBlack = ColorUtil.toFloat4(0);

    for (int i = 0; i < getNumElements(); ++i) {

      final int elementY = contentYOffset + i * this.slotHeight;

      if (elementY <= maxY && elementY + drawHeight >= minY) {

        if (showSelectionBox && i == selectedIndex) {
          RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
          RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
          RenderSystem.disableTexture();

          renderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
          renderer.vertex(minX, elementY + drawHeight + 2, 0.0D).uv(0.0f, 1.0f).color(col.x, col.y, col.z, col.w).endVertex();
          renderer.vertex(maxX, elementY + drawHeight + 2, 0.0D).uv(1.0f, 1.0f).color(col.x, col.y, col.z, col.w).endVertex();
          renderer.vertex(maxX, elementY - 2, 0.0D).uv(1.0f, 0.0f).color(col.x, col.y, col.z, col.w).endVertex();
          renderer.vertex(minX, elementY - 2, 0.0D).uv(0.0f, 0.0f).color(col.x, col.y, col.z, col.w).endVertex();
          renderer.color(0, 0, 0, 255);
          renderer.vertex(minX + 1, elementY + drawHeight + 1, 0.0D).uv(0.0f, 1.0f).color(colBlack.x, colBlack.y, colBlack.z, colBlack.w).endVertex();
          renderer.vertex(maxX - 1, elementY + drawHeight + 1, 0.0D).uv(1.0f, 1.0f).color(colBlack.x, colBlack.y, colBlack.z, colBlack.w).endVertex();
          renderer.vertex(maxX - 1, elementY - 1, 0.0D).uv(1.0f, 0.0f).color(colBlack.x, colBlack.y, colBlack.z, colBlack.w).endVertex();
          renderer.vertex(minX + 1, elementY - 1, 0.0D).uv(0.0f, 0.0f).color(colBlack.x, colBlack.y, colBlack.z, colBlack.w).endVertex();
          renderer.end();

          RenderSystem.enableTexture();
        }

        drawElement(i, minX, elementY, drawHeight, renderer);
      }
    }

    GL11.glDisable(GL11.GL_SCISSOR_TEST);

    RenderSystem.disableDepthTest();
    //RenderSystem.disableAlphaTest();
    RenderSystem.enableBlend();
    RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    //RenderSystem.shadeModel(GL11.GL_SMOOTH);
    RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
    RenderSystem.disableTexture();

    boolean renderBorder = true;
    if (renderBorder) {
      final Vec4f colBorder = ColorUtil.toFloat4(0xFF000000);
      renderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
      renderer.vertex(this.minX, this.minY + margin, 0.0D).color(colBlack.x, colBlack.y, colBlack.z, colBlack.w).uv(0.0f, 1.0f).endVertex();
      renderer.vertex(this.maxX, this.minY + margin, 0.0D).color(colBlack.x, colBlack.y, colBlack.z, colBlack.w).uv(1.0f, 1.0f).endVertex();
      renderer.vertex(this.maxX, this.minY, 0.0D).uv(1.0f, 0.0f).color(colBorder.x, colBorder.y, colBorder.z, colBorder.w).endVertex();
      renderer.vertex(this.minX, this.minY, 0.0D).uv(0.0f, 0.0f).color(colBorder.x, colBorder.y, colBorder.z, colBorder.w).endVertex();
      renderer.vertex(this.minX, this.maxY, 0.0D).uv(0.0f, 1.0f).color(colBorder.x, colBorder.y, colBorder.z, colBorder.w).endVertex();
      renderer.vertex(this.maxX, this.maxY, 0.0D).uv(1.0f, 1.0f).color(colBorder.x, colBorder.y, colBorder.z, colBorder.w).endVertex();
      renderer.vertex(this.maxX, this.maxY - margin + 1, 0.0D).uv(1.0f, 0.0f).color(colBlack.x, colBlack.y, colBlack.z, colBlack.w).endVertex();
      renderer.vertex(this.minX, this.maxY - margin + 1, 0.0D).uv(0.0f, 0.0f).color(colBlack.x, colBlack.y, colBlack.z, colBlack.w).endVertex();
      renderer.end();
    }

    renderScrollBar(renderer);
    RenderSystem.enableTexture();
    //RenderSystem.enableAlphaTest();
    RenderSystem.disableBlend();
    //RenderSystem.shadeModel(GL11.GL_FLAT);
  }

  protected void renderScrollBar(@Nonnull BufferBuilder renderer) {

    final int contentHeightOverBounds = getContentOverhang();
    if (contentHeightOverBounds > 0) {

      int clear = (maxY - minY) * (maxY - minY) / getContentHeight();

      if (clear < 32) {
        clear = 32;
      }

      if (clear > maxY - minY - 8) {
        clear = maxY - minY - 8;
      }

      int y = (int) this.amountScrolled * (maxY - minY - clear) / contentHeightOverBounds + minY;
      if (y < minY) {
        y = minY;
      }

      final Vec4f col = ColorUtil.toFloat4(0xFF000000);

      RenderSystem.disableTexture();
      final int scrollBarMinX = getScrollBarX();
      final int scrollBarMaxX = scrollBarMinX + 6;
      
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      
      renderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

      renderer.vertex(scrollBarMinX, maxY, 0.0D).uv(0.0f, 1.0f).color(col.x, col.y, col.z, col.w).endVertex();
      renderer.vertex(scrollBarMaxX, maxY, 0.0D).uv(1.0f, 1.0f).color(col.x, col.y, col.z, col.w).endVertex();
      renderer.vertex(scrollBarMaxX, minY, 0.0D).uv(1.0f, 0.0f).color(col.x, col.y, col.z, col.w).endVertex();
      renderer.vertex(scrollBarMinX, minY, 0.0D).uv(0.0f, 0.0f).color(col.x, col.y, col.z, col.w).endVertex();

      renderer.vertex(scrollBarMinX, y + clear, 0.0D).uv(0.0f, 1.0f).color(0.3f, 0.3f, 0.3f, 1).endVertex();
      renderer.vertex(scrollBarMaxX, y + clear, 0.0D).uv(1.0f, 1.0f).color(0.3f, 0.3f, 0.3f, 1).endVertex();
      renderer.vertex(scrollBarMaxX, y, 0.0D).uv(1.0f, 0.0f).color(0.3f, 0.3f, 0.3f, 1).endVertex();
      renderer.vertex(scrollBarMinX, y, 0.0D).uv(0.0f, 0.0f).color(0.3f, 0.3f, 0.3f, 1).endVertex();

      renderer.vertex(scrollBarMinX, y + clear - 1, 0.0D).uv(0.0f, 1.0f).color(0.7f, 0.7f, 0.7f, 1).endVertex();
      renderer.vertex(scrollBarMaxX - 1, y + clear - 1, 0.0D).uv(1.0f, 1.0f).color(0.7f, 0.7f, 0.7f, 1).endVertex();
      renderer.vertex(scrollBarMaxX - 1, y, 0.0D).uv(1.0f, 0.0f).color(0.7f, 0.7f, 0.7f, 1).endVertex();
      renderer.vertex(scrollBarMinX, y, 0.0D).uv(0.0f, 0.0f).color(0.7f, 0.7f, 0.7f, 1).endVertex();

      renderer.end();
      RenderSystem.enableTexture();
    }
  }

  @Override
  public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
    if (button == 0) {
      processMouseBown((int) mouseX, (int) mouseY);
    }
    return GuiEventListener.super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
  }
  
  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    if (delta != 0) {
      amountScrolled += Math.signum(delta) * slotHeight / 2;
    }
    initialClickY = -1.0F;
    return GuiEventListener.super.mouseScrolled(mouseX, mouseY, delta);
  }

  private void processMouseBown(int mX, int mY) {
    int contentHeightOverBounds;
    if (initialClickY == -1.0F) {

      if (mY >= minY && mY <= maxY && mX >= minX && mX <= maxX + 6) {

        boolean clickInBounds = true;

        int y = mY - minY + (int) amountScrolled - margin;
        int mouseOverElement = y / slotHeight;

        if (mX >= minX && mX <= maxX && mouseOverElement >= 0 && y >= 0 && mouseOverElement < getNumElements()) {
          boolean doubleClick = mouseOverElement == selectedIndex && System.nanoTime() - lastClickedTime < 250L;
          if (elementClicked(mouseOverElement, doubleClick, mX, y % slotHeight)) {
            setSelection(mouseOverElement);
          }
          lastClickedTime = System.nanoTime();

        } else if (mX >= minX && mX <= maxX && y < 0) {
          clickInBounds = false;
        }

        int scrollBarMinX = getScrollBarX();
        int scrollBarMaxX = scrollBarMinX + 6;
        if (mX >= scrollBarMinX && mX <= scrollBarMaxX) {

          scrollMultiplier = -1.0F;
          contentHeightOverBounds = getContentOverhang();

          if (contentHeightOverBounds < 1) {
            contentHeightOverBounds = 1;
          }

          int empty = (int) ((float) ((maxY - minY) * (maxY - minY)) / (float) getContentHeight());
          if (empty < 32) {
            empty = 32;
          }
          if (empty > maxY - minY - 8) {
            empty = maxY - minY - 8;
          }
          scrollMultiplier /= (float) (maxY - minY - empty) / (float) contentHeightOverBounds;

        } else {
          scrollMultiplier = 1.0F;
        }

        if (clickInBounds) {
          initialClickY = mY;
        } else {
          initialClickY = -2.0F;
        }

      } else {
        initialClickY = -2.0F;
      }

    } else if (initialClickY >= 0.0F) {
      // Scrolling
      amountScrolled -= (mY - initialClickY) * scrollMultiplier;
      initialClickY = mY;
    }
  }

  protected int getScrollBarX() {
    return minX + width - 6;
  }

  protected void drawContainerBackground(@Nonnull BufferBuilder renderer) {

    Vec3f col = ColorUtil.toFloat(2105376);
    RenderSystem.setShader(GameRenderer::getPositionColorShader);
    RenderSystem.setShaderColor(col.x, col.y, col.z, 1.0F);
    RenderSystem.disableTexture();

    renderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
    renderer.vertex(minX, maxY + margin + 10, 0.0D).endVertex();
    renderer.vertex(maxX, maxY + margin + 10, 0.0D).endVertex();
    renderer.vertex(maxX, minY, 0.0D).endVertex();
    renderer.vertex(minX, minY, 0.0D).endVertex();
    renderer.end();
    RenderSystem.enableTexture();

  }

}
