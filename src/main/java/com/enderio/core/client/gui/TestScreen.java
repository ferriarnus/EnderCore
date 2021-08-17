package com.enderio.core.client.gui;

import com.enderio.core.api.client.render.IWidgetIcon;
import com.enderio.core.client.gui.button.CheckBox;
import com.enderio.core.client.gui.button.ColorButton;
import com.enderio.core.client.gui.button.CycleButton;
import com.enderio.core.client.render.EnderWidget;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class TestScreen extends BaseContainerScreen<RepairContainer> {

    CheckBox box = new CheckBox(this, 0, 40);
    ColorButton color = new ColorButton(this, 0, 70);
    CycleButton cycle = new CycleButton(this, 0, 100, Cycleable.class);

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent e) {
        if (e.getEntity() instanceof PlayerEntity) {
            Minecraft.getInstance().displayGuiScreen(new TestScreen());
        }
    }
    protected TestScreen() {
        super(new RepairContainer(10, Minecraft.getInstance().player.inventory) {
            @Override
            public boolean canInteractWith(PlayerEntity playerEntity) {
                return true;
            }
        }, Minecraft.getInstance().player.inventory,StringTextComponent.EMPTY);
        xSize = 200;
        ySize = 200;


        box.setSelectedTooltip(new StringTextComponent("testselected"));
        box.setUnselectedTooltip(new StringTextComponent("unselected"));

        color.setTooltipPrefix(new StringTextComponent("Channel:"));
    }

    @Override
    public void init() {
        super.init();
        box.onGuiInit();
        color.onGuiInit();
        cycle.onGuiInit();
    }

    @Nonnull
    @Override
    protected ResourceLocation getGuiTexture() {
        return null;
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
        return new GhostSlotHandler();
    }

    private enum Cycleable implements CycleButton.ICycleEnum {
        ONE(EnderWidget.MINUS),
        TWO(EnderWidget.PLUS),
        THREE(EnderWidget.BUTTON_CHECKED);

        IWidgetIcon icon;
        Cycleable(IWidgetIcon icon) {
            this.icon = icon;
        }

        @Nonnull
        @Override
        public IWidgetIcon getIcon() {
            return icon;
        }

        @Nonnull
        @Override
        public List<ITextComponent> getTooltipLines() {
            ArrayList<ITextComponent> tooltips = new ArrayList<>();
            tooltips.add(new StringTextComponent("12  " + name()));
            return tooltips;
        }
    }
}
