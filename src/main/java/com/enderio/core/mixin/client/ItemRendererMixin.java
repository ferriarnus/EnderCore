package com.enderio.core.mixin.client;

import com.enderio.core.common.interfaces.IOverlayRenderAware;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
  @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getInstance()Lnet/minecraft/client/Minecraft;", ordinal = 1), method = "renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", cancellable = true)
  private void renderItemOverlayIntoGUI(Font fr, ItemStack stack, int xPosition, int yPosition, String text, CallbackInfo ci) {
    if (!stack.isEmpty()) {
      if (stack.getItem() instanceof IOverlayRenderAware) {
        ((IOverlayRenderAware) stack.getItem()).renderItemOverlayIntoGUI(stack, xPosition, yPosition);
      }
    }
  }
}
