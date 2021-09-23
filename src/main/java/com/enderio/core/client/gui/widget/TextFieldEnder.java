package com.enderio.core.client.gui.widget;

import java.lang.reflect.Field;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.api.client.gui.IHideable;
import com.google.common.base.Strings;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class TextFieldEnder extends EditBox implements IHideable {

  public interface ICharFilter {

    boolean passesFilter(@Nonnull TextFieldEnder tf, char c);
  }

  public static final ICharFilter FILTER_NUMERIC = (tf, c) -> Character.isDigit(c) || c == '-' && Strings.isNullOrEmpty(tf.getValue());

  public static final ICharFilter FILTER_ALPHABETICAL = (tf, c) -> Character.isLetter(c);

  public static final ICharFilter FILTER_ALPHANUMERIC = (tf, c) -> FILTER_NUMERIC.passesFilter(tf, c) || FILTER_ALPHABETICAL.passesFilter(tf, c);

  private int xOrigin;
  private int yOrigin;
  private @Nullable ICharFilter filter;

  private static Field canLoseFocus;

  static {
    try {
      canLoseFocus = ObfuscationReflectionHelper.findField(EditBox.class, "canLoseFocus");
      canLoseFocus.setAccessible(true);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public TextFieldEnder(@Nonnull Font fnt, int x, int y, int width, int height, Component title) {
    this(fnt, x, y, width, height, title, null);
  }

  public TextFieldEnder(@Nonnull Font fnt, int x, int y, int width, int height, Component title, @Nullable ICharFilter charFilter) {
    super(fnt, x, y, width, height, title);
    xOrigin = x;
    yOrigin = y;
    filter = charFilter;
  }

  public void init(@Nonnull IGuiScreen gui) {
    this.x = xOrigin + gui.getGuiRootLeft();
    this.y = yOrigin + gui.getGuiRootTop();
  }

  public TextFieldEnder setCharFilter(@Nullable ICharFilter filter) {
    this.filter = filter;
    return this;
  }



  @Override
  public boolean charTyped(char codePoint, int modifiers) {
    final ICharFilter filter2 = filter;
    if (filter2 == null || filter2.passesFilter(this, codePoint) || isSpecialChar(codePoint, modifiers)) {
      return super.charTyped(codePoint, modifiers);
    }
    return false;
  }

  public static boolean isSpecialChar(char c, int key) {
    // taken from the giant switch statement in GuiTextField
    return c == 1 || c == 3 || c == 22 || c == 24 || key == 14 || key == 199 || key == 203 || key == 205 || key == 207 || key == 211;
  }

  public boolean getCanLoseFocus() {
    try {
      return canLoseFocus.getBoolean(this);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public boolean contains(double mouseX, double mouseY) {
    return mouseX >= this.x && mouseX < this.x + width && mouseY >= this.y && mouseY < this.y + height;
  }

  @Override
  public boolean isVisible() {
    return isVisible();
  }

  @Override
  public void setIsVisible(boolean visible) {
    setVisible(visible);
  }

  public void setXOrigin(int xOrigin) {
    this.xOrigin = xOrigin;
  }

  public void setYOrigin(int yOrigin) {
    this.yOrigin = yOrigin;
  }

  public @Nullable Integer getInteger() {
    String text = getValue();
    try {
      return Integer.parseInt(text);
    } catch (Exception e) {
      return null;
    }
  }
}
