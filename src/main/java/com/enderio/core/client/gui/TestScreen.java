package com.enderio.core.client.gui;

import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.client.gui.button.ColorButton;
import com.enderio.core.client.gui.widget.TooltipWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import java.io.IOException;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class TestScreen extends Screen implements IGuiScreen {

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent e) {
        if (e.getEntity() instanceof PlayerEntity) {
            Minecraft.getInstance().displayGuiScreen(new TestScreen());
        }
    }
    protected TestScreen() {
        super(StringTextComponent.EMPTY);
    }

    @Override
    protected void init() {
        super.init();
        addButton(new ColorButton(this, 100,100));
    }

    @Override
    public void addToolTip(@Nonnull TooltipWidget toolTip) {
    }

    @Override
    public boolean removeToolTip(@Nonnull TooltipWidget toolTip) {
        return false;
    }

    @Override
    public void clearToolTips() {
    }

    @Override
    public int getGuiRootLeft() {
        return 100;
    }

    @Override
    public int getGuiRootTop() {
        return 100;
    }

    @Override
    public int getGuiXSize() {
        return 400;
    }

    @Override
    public int getGuiYSize() {
        return 400;
    }

    @Nonnull
    @Override
    public <T extends Button> T addGuiButton(@Nonnull T button) {
        return button;
    }

    @Override
    public void removeButton(@Nonnull Button button) {
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
    public void doActionPerformed(@Nonnull Button but) throws IOException {
    }

    @Nonnull
    @Override
    public GhostSlotHandler getGhostSlotHandler() {
        return null;
    }
}
