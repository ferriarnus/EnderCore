package com.enderio.core.client.gui.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;

public class BaseButton extends Button {
    private static final IPressable DUD_PRESSABLE = button -> { };

    public BaseButton(int x, int y, int width, int height, ITextComponent title) {
        super(x, y, width, height, title, DUD_PRESSABLE);
    }

    public BaseButton(int x, int y, int width, int height, ITextComponent title, IPressable pressedAction) {
        super(x, y, width, height, title, pressedAction);
    }

    public BaseButton(int x, int y, int width, int height, ITextComponent title, ITooltip onTooltip) {
        super(x, y, width, height, title, DUD_PRESSABLE, onTooltip);
    }

    public BaseButton(int x, int y, int width, int height, ITextComponent title, IPressable pressedAction, ITooltip onTooltip) {
        super(x, y, width, height, title, pressedAction, onTooltip);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isHovered() {
        if (!isActive())
            return false;
        return super.isHovered();
    }

    @Override
    public void onPress() {
        if (isActive())
            super.onPress();
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return isActive() && super.clicked(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isActive()) {
            if (super.mouseClicked(mouseX, mouseY, button))
                return true;
            if (this.visible && clicked(mouseX, mouseY)) {
                if (buttonPressed(mouseX, mouseY, button)) {
                    playDownSound(Minecraft.getInstance().getSoundHandler());
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Override this to handle mouse clicks with other buttons than the left
     *
     * @param mouseX
     *          X coordinate of mouse click
     * @param mouseY
     *          Y coordinate of mouse click
     * @param button
     *          the mouse button - only called for buttons other as {@link net.minecraft.client.gui.widget.Widget#isValidClickButton}
     * @return true if the mouse click is handled
     */
    public boolean buttonPressed(double mouseX, double mouseY, int button) {
        return false;
    }
}
