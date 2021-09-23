package com.enderio.core.client.gui.widget;

import java.awt.Rectangle;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import org.lwjgl.opengl.GL11;

import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.api.client.gui.IHideable;
import com.enderio.core.api.client.render.IWidgetIcon;
import com.enderio.core.client.render.EnderWidget;

import net.minecraft.client.Minecraft;

public class VScrollbar implements IHideable {

  protected final @Nonnull IGuiScreen gui;

  protected int xOrigin;
  protected int yOrigin;
  protected int height;

  protected int x;

  protected int y;

  protected @Nonnull Rectangle wholeArea;
  protected @Nonnull Rectangle btnUp;
  protected @Nonnull Rectangle btnDown;
  protected @Nonnull Rectangle thumbArea;

  protected int scrollPos;
  protected int scrollMax;

  protected boolean pressedUp;
  protected boolean pressedDown;
  protected boolean pressedThumb;
  protected int scrollDir;
  protected long timeNextScroll;

  protected boolean visible = true;

  public VScrollbar(@Nonnull IGuiScreen gui, int xOrigin, int yOrigin, int height) {
    this.gui = gui;
    this.xOrigin = xOrigin;
    this.yOrigin = yOrigin;
    this.height = height;
    wholeArea = btnUp = btnDown = thumbArea = new Rectangle();
  }

  public void adjustPosition() {
    x = xOrigin + gui.getGuiRootLeft();
    y = yOrigin + gui.getGuiRootTop();
    wholeArea = new Rectangle(x, y, EnderWidget.VSCROLL_THUMB_OFF.width, height);
    btnUp = new Rectangle(x, y, EnderWidget.UP_ARROW_OFF.width, EnderWidget.UP_ARROW_OFF.height);
    btnDown = new Rectangle(x, y + Math.max(0, height - EnderWidget.DOWN_ARROW_OFF.height), EnderWidget.DOWN_ARROW_OFF.width,
        EnderWidget.DOWN_ARROW_OFF.height);
    thumbArea = new Rectangle(x, y + btnUp.height, EnderWidget.VSCROLL_THUMB_OFF.width, Math.max(0, height - (btnUp.height + btnDown.height)));
  }

  public int getScrollPos() {
    return scrollPos;
  }

  public void setScrollPos(int scrollPos) {
    this.scrollPos = limitPos(scrollPos);
  }

  public void scrollBy(int amount) {
    setScrollPos(scrollPos + amount);
  }

  public int getScrollMax() {
    return scrollMax;
  }

  public void setScrollMax(int scrollMax) {
    this.scrollMax = scrollMax;
    setScrollPos(scrollPos);
  }

  public void drawScrollbar(PoseStack matrixStack, int mouseX, int mouseY) {
    if (visible) {
      boolean hoverUp = btnUp.contains(mouseX, mouseY);
      boolean hoverDown = btnDown.contains(mouseX, mouseY);

      IWidgetIcon iconUp;
      if (pressedUp) {
        iconUp = hoverUp ? EnderWidget.UP_ARROW_HOVER_ON : EnderWidget.UP_ARROW_ON;
      } else {
        iconUp = hoverUp ? EnderWidget.UP_ARROW_HOVER_OFF : EnderWidget.UP_ARROW_OFF;
      }

      IWidgetIcon iconDown;
      if (pressedDown) {
        iconDown = hoverDown ? EnderWidget.DOWN_ARROW_HOVER_ON : EnderWidget.DOWN_ARROW_ON;
      } else {
        iconDown = hoverDown ? EnderWidget.DOWN_ARROW_HOVER_OFF : EnderWidget.DOWN_ARROW_OFF;
      }

      if (scrollDir != 0) {
        long time = Util.getMillis();
        if (timeNextScroll - time <= 0) {
          timeNextScroll = time + 100;
          scrollBy(scrollDir);
        }
      }

      RenderSystem.setShaderTexture(0, EnderWidget.TEXTURE);
      GL11.glPushAttrib(GL11.GL_ENABLE_BIT); // TODO waiting on forge for a bug fix of pushAtrrib
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
      RenderSystem.setShaderColor(1, 1, 1, 1);

      final BufferBuilder renderer = Tesselator.getInstance().getBuilder();
      renderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

      iconUp.getMap().render(matrixStack,iconUp, btnUp.x, btnUp.y, false);
      iconDown.getMap().render(matrixStack,iconDown, btnDown.x, btnDown.y, false);

      if (getScrollMax() > 0) {
        int thumbPos = getThumbPosition();
        boolean hoverThumb = thumbArea.contains(mouseX, mouseY) && mouseY >= thumbPos && mouseY < thumbPos + EnderWidget.VSCROLL_THUMB_OFF.height;

        EnderWidget iconThumb;
        if (pressedThumb) {
          iconThumb = EnderWidget.VSCROLL_THUMB_HOVER_ON;
        } else {
          iconThumb = hoverThumb ? EnderWidget.VSCROLL_THUMB_HOVER_OFF : EnderWidget.VSCROLL_THUMB_OFF;
        }
        iconThumb.getMap().render(matrixStack,iconThumb, thumbArea.x, thumbPos, 100, false);
      }

      Tesselator.getInstance().end();
      GL11.glPopAttrib(); // TODO waiting on forge for a bugfix
    }
  }

  public boolean mouseClicked(double mX, double mY, int button) {
    if (button == 0) {
      if (getScrollMax() > 0 && thumbArea.contains(mX, mY)) {
        int thumbPos = getThumbPosition();
        pressedUp = mY < thumbPos;
        pressedDown = mY >= thumbPos + EnderWidget.VSCROLL_THUMB_OFF.height;
        pressedThumb = !pressedUp && !pressedDown;
      } else {
        pressedUp = btnUp.contains(mX, mY);
        pressedDown = btnDown.contains(mX, mY);
        pressedThumb = false;
      }

      scrollDir = (pressedDown ? 1 : 0) - (pressedUp ? 1 : 0);
      if (scrollDir != 0) {
        timeNextScroll = Util.getMillis() + 200;
        scrollBy(scrollDir);
      }
    }
    return isDragActive();
  }

  public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
    if (pressedThumb) {
      double pos = mouseY - (thumbArea.y + EnderWidget.VSCROLL_THUMB_OFF.height / 2);
      int len = thumbArea.height - EnderWidget.VSCROLL_THUMB_OFF.height;
      if (len > 0) {
        setScrollPos((int) Math.round(pos * (float) getScrollMax() / len));
      }
      return true;
    }
    return false;
  }

  public void mouseReleased(double mX, double mY, int button) {
    pressedUp = false;
    pressedDown = false;
    pressedThumb = false;
    scrollDir = 0;
  }

  public void mouseScrolled(double mX, double mY, double delta) {
    if (!isDragActive()) {
      scrollBy(-Integer.signum((int) Math.round(delta))); // TODO: Ugly, maybe just write a sign func?
    }
  }

  public boolean isDragActive() {
    return pressedUp || pressedDown || pressedThumb;
  }

  protected int getThumbPosition() {
    return thumbArea.y + (thumbArea.height - EnderWidget.VSCROLL_THUMB_OFF.height) * scrollPos / getScrollMax();
  }

  protected int limitPos(int pos) {
    return Math.max(0, Math.min(pos, getScrollMax()));
  }

  @Override
  public void setIsVisible(boolean visible) {
    this.visible = visible;
  }

  @Override
  public boolean isVisible() {
    return visible;
  }
}
