package com.enderio.core.client.render;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import org.lwjgl.opengl.GL11;

import com.enderio.core.common.BlockEntityBase;

import net.minecraft.world.level.block.Block;

public abstract class ManagedTESR<T extends BlockEntityBase> implements BlockEntityRenderer<T> {

  protected final @Nullable Block block;

  public ManagedTESR(BlockEntityRenderDispatcher rendererDispatcherIn, @Nullable Block block) {
    this.block = block;
  }

  @Override
  public void render(@Nonnull T tileEntityIn, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
    if (tileEntityIn != null && tileEntityIn.hasLevel() && !tileEntityIn.isRemoved()) {
      final BlockState blockState = tileEntityIn.getLevel().getBlockState(tileEntityIn.getBlockPos());
      final int renderPass = Minecraft.getInstance().renderOnThread() ? 1 : 0;

      if ((block == null || block == blockState.getBlock()) && shouldRender(tileEntityIn, blockState, renderPass)) {

        //RenderSystem.disableLighting();
        if (renderPass == 0) {
          RenderSystem.disableBlend();
          RenderSystem.depthMask(true);
        } else {
          RenderSystem.enableBlend();
          RenderSystem.depthMask(false);
          RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }

        RenderUtil.bindBlockTexture();
        matrixStackIn.pushPose();
        matrixStackIn.translate(tileEntityIn.getBlockPos().getX(), tileEntityIn.getBlockPos().getY(), tileEntityIn.getBlockPos().getZ());
        renderTileEntity(tileEntityIn, blockState, partialTicks);
        matrixStackIn.popPose();
      }
    } else if (tileEntityIn == null) {
      renderItem();
    }
  }

  protected abstract void renderTileEntity(@Nonnull T te, @Nonnull BlockState blockState, float partialTicks);

  protected boolean shouldRender(@Nonnull T te, @Nonnull BlockState blockState, int renderPass) {
    return true;
  }

  protected void renderItem() {
  }

}
