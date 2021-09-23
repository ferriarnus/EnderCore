package com.enderio.core.client.handlers;

import javax.annotation.Nonnull;

import com.enderio.core.EnderCore;
import com.enderio.core.common.fluid.EnderFluidBlock;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.tags.FluidTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class FluidVisualsHandler {

  @SubscribeEvent
  public static void onFOVModifier(@Nonnull EntityViewRenderEvent.FOVModifier event) {
    if (event.getInfo().getBlockAtCamera().getBlock() instanceof EnderFluidBlock) {
      event.setFOV(event.getFOV() * 60.0F / 70.0F);
    }
  }

  private static final @Nonnull ResourceLocation RES_UNDERFLUID_OVERLAY = new ResourceLocation(EnderCore.DOMAIN, "textures/misc/underfluid.png");

  @SubscribeEvent
  public static void onRenderBlockOverlay(RenderBlockOverlayEvent event) {
    if (event.getOverlayType() == OverlayType.WATER) {
      final Player player = event.getPlayer();
      // the event has the wrong BlockPos (entity center instead of eyes)
      final BlockPos blockpos = new BlockPos(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
      final Block block = player.level.getBlockState(blockpos).getBlock();

      if (block instanceof EnderFluidBlock) {
        float fogColorRed = ((EnderFluidBlock) block).getFogColorRed();
        float fogColorGreen = ((EnderFluidBlock) block).getFogColorGreen();
        float fogColorBlue = ((EnderFluidBlock) block).getFogColorBlue();

        RenderSystem.setShaderTexture(0, RES_UNDERFLUID_OVERLAY);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuilder();
        float f = player.getBrightness();
        RenderSystem.setShaderColor(f * fogColorRed, f * fogColorGreen, f * fogColorBlue, 0.5F);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        event.getMatrixStack().pushPose();
        float f7 = -player.yRotO / 64.0F;
        float f8 = player.xRotO / 64.0F;
        vertexbuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        vertexbuffer.vertex(-1.0D, -1.0D, -0.5D).uv(4.0F + f7, 4.0F + f8).endVertex();
        vertexbuffer.vertex(1.0D, -1.0D, -0.5D).uv(0.0F + f7, 4.0F + f8).endVertex();
        vertexbuffer.vertex(1.0D, 1.0D, -0.5D).uv(0.0F + f7, 0.0F + f8).endVertex();
        vertexbuffer.vertex(-1.0D, 1.0D, -0.5D).uv(4.0F + f7, 0.0F + f8).endVertex();
        tessellator.end();
        event.getMatrixStack().popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

        event.setCanceled(true);
      }
    }
  }

  @SubscribeEvent
  public static void onFogDensity(@Nonnull EntityViewRenderEvent.FogDensity event) throws IllegalArgumentException {
    BlockState blockState = event.getInfo().getBlockAtCamera();
    if (blockState.getBlock() instanceof EnderFluidBlock) {
      final Entity entity = event.getInfo().getEntity();
      final boolean cloudFog = event.getType() == FogRenderer.FogMode.FOG_SKY;

      // again the event is fired at a bad location...
      if (cloudFog || entity instanceof LivingEntity && ((LivingEntity) entity).hasEffect(MobEffects.BLINDNESS)) {
        return;
      }

      //RenderSystem.fogMode(GlStateManager.FogMode.EXP);

      if (entity instanceof LivingEntity) {
        if (((LivingEntity) entity).hasEffect(MobEffects.WATER_BREATHING)) {
          event.setDensity(0.01F);
        } else {
          event.setDensity(0.1F - EnchantmentHelper.getRespiration((LivingEntity) entity) * 0.03F);
        }
      } else {
        event.setDensity(0.1F);
      }
    }
  }

  @SubscribeEvent
  public static void onFogColor(EntityViewRenderEvent.FogColors event) throws IllegalArgumentException {
    BlockState blockState = event.getInfo().getBlockAtCamera();
    if (blockState.getBlock() instanceof EnderFluidBlock) {

      float fogColorRed = ((EnderFluidBlock) blockState.getBlock()).getFogColorRed();
      float fogColorGreen = ((EnderFluidBlock) blockState.getBlock()).getFogColorGreen();
      float fogColorBlue = ((EnderFluidBlock) blockState.getBlock()).getFogColorBlue();

      // Fields for this hateful mess
      Camera activeRenderInfoIn = event.getInfo();
      ClientLevel worldIn = ((ClientLevel) event.getInfo().getEntity().getCommandSenderWorld());
      FluidState fluidstate = event.getInfo().getBlockAtCamera().getFluidState();

      // Copied with hate
      double d0 = activeRenderInfoIn.getPosition().y * worldIn.getLevelData().getClearColorScale();
      if (activeRenderInfoIn.getEntity() instanceof LivingEntity && ((LivingEntity)activeRenderInfoIn.getEntity()).hasEffect(MobEffects.BLINDNESS)) {
        int i2 = ((LivingEntity)activeRenderInfoIn.getEntity()).getEffect(MobEffects.BLINDNESS).getDuration();
        if (i2 < 20) {
          d0 *= (double)(1.0F - (float)i2 / 20.0F);
        } else {
          d0 = 0.0D;
        }
      }

      if (d0 < 1.0D && !fluidstate.is(FluidTags.LAVA)) {
        if (d0 < 0.0D) {
          d0 = 0.0D;
        }

        d0 = d0 * d0;
        fogColorRed = (float)((double)fogColorRed * d0);
        fogColorGreen = (float)((double)fogColorGreen * d0);
        fogColorBlue = (float)((double)fogColorBlue * d0);
      }

      GameRenderer gameRendererIn = event.getRenderer();
      float bossColorModifier = gameRendererIn.getDarkenWorldAmount((float) event.getRenderPartialTicks());
      if (bossColorModifier > 0.0F) {
        fogColorRed = fogColorRed * (1.0F - bossColorModifier) + fogColorRed * 0.7F * bossColorModifier;
        fogColorGreen = fogColorGreen * (1.0F - bossColorModifier) + fogColorGreen * 0.6F * bossColorModifier;
        fogColorBlue = fogColorBlue * (1.0F - bossColorModifier) + fogColorBlue * 0.6F * bossColorModifier;
      }

      if (fluidstate.is(FluidTags.WATER)) {
        float f6 = 0.0F;
        if (activeRenderInfoIn.getEntity() instanceof LocalPlayer) {
          LocalPlayer clientplayerentity = (LocalPlayer)activeRenderInfoIn.getEntity();
          f6 = clientplayerentity.getWaterVision();
        }

        float f9 = Math.min(1.0F / fogColorRed, Math.min(1.0F / fogColorGreen, 1.0F / fogColorBlue));
        // Forge: fix MC-4647 and MC-10480
        if (Float.isInfinite(f9)) f9 = Math.nextAfter(f9, 0.0);
        fogColorRed = fogColorRed * (1.0F - f6) + fogColorRed * f9 * f6;
        fogColorGreen = fogColorGreen * (1.0F - f6) + fogColorGreen * f9 * f6;
        fogColorBlue = fogColorBlue * (1.0F - f6) + fogColorBlue * f9 * f6;
      } else if (activeRenderInfoIn.getEntity() instanceof LivingEntity && ((LivingEntity)activeRenderInfoIn.getEntity()).hasEffect(MobEffects.NIGHT_VISION)) {
        float f7 = GameRenderer.getNightVisionScale((LivingEntity)activeRenderInfoIn.getEntity(), (float) event.getRenderPartialTicks());
        float f10 = Math.min(1.0F / fogColorRed, Math.min(1.0F / fogColorGreen, 1.0F / fogColorBlue));
        // Forge: fix MC-4647 and MC-10480
        if (Float.isInfinite(f10)) f10 = Math.nextAfter(f10, 0.0);
        fogColorRed = fogColorRed * (1.0F - f7) + fogColorRed * f10 * f7;
        fogColorGreen = fogColorGreen * (1.0F - f7) + fogColorGreen * f10 * f7;
        fogColorBlue = fogColorBlue * (1.0F - f7) + fogColorBlue * f10 * f7;
      }

      event.setRed(fogColorRed);
      event.setGreen(fogColorGreen);
      event.setBlue(fogColorBlue);
    }
  }
}
