package com.enderio.core.client.handlers;

import java.util.Map;

import com.enderio.core.EnderCore;
import com.enderio.core.api.common.enchantment.IAdvancedEnchantment;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class EnchantTooltipHandler {

  @SubscribeEvent
  public static void handleTooltip(ItemTooltipEvent event) {
    if (event.getItemStack().getTag() != null) {
      Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(event.getItemStack());

      for (Enchantment enchant : enchantments.keySet()) {
        if (enchant instanceof IAdvancedEnchantment) {
          for (int i = 0; i < event.getToolTip().size(); i++) {
            if (event.getToolTip().get(i).getString().contains(EnderCore.lang.localizeExact(enchant.getDescriptionId()))) {
              for (String s : ((IAdvancedEnchantment) enchant).getTooltipDetails(event.getItemStack())) {
                event.getToolTip().add(i + 1, new TextComponent(ChatFormatting.DARK_GRAY.toString() + ChatFormatting.ITALIC + "  - " + s));
                i++;
              }
            }
          }
        }
      }
    }
  }

}
