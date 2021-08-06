package com.enderio.core.api.client.gui;

import net.minecraft.client.gui.IGuiEventListener;

import java.awt.Rectangle;

import javax.annotation.Nonnull;

public interface IGuiOverlay extends IHideable, IGuiEventListener {

    void init(@Nonnull IGuiScreen screen);

    @Nonnull Rectangle getBounds();

    void draw(int mouseX, int mouseY, float partialTick);

    boolean isMouseInBounds(double mouseX, double mouseY);

    @Override
    default boolean isMouseOver(double mouseX, double mouseY) {
        return isMouseInBounds(mouseX, mouseY) && isVisible();
    }

    void onClose();

}
