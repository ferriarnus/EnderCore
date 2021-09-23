package com.enderio.core.common.fluid;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.LiquidBlock;

import java.util.function.Supplier;

/**
 * Represents a fluid in block form.
 * This contains information about the fog color.
 */
public class EnderFluidBlock extends LiquidBlock {
  private float fogColorRed = 1f;
  private float fogColorGreen = 1f;
  private float fogColorBlue = 1f;

  public EnderFluidBlock(Supplier<? extends EnderFlowingFluid> fluidSupplier, BlockBehaviour.Properties builder, int fogColor) {
    super(fluidSupplier, builder);

    // Darken fog color to fit the fog rendering
    float dim = 1;
    while (this.fogColorRed > .2f || this.fogColorGreen > .2f || this.fogColorBlue > .2f) {
      this.fogColorRed = ((fogColor >> 16) & 0xFF) / 255f * dim;
      this.fogColorGreen = ((fogColor >> 8) & 0xFF) / 255f * dim;
      this.fogColorBlue = (fogColor & 0xFF) / 255f * dim;
      dim *= .9f;
    }
  }

  public float getFogColorRed() {
    return fogColorRed;
  }

  public float getFogColorGreen() {
    return fogColorGreen;
  }

  public float getFogColorBlue() {
    return fogColorBlue;
  }

  // TODO: This is what we'd need if we are going to enable full ticking for fluid blocks. Right now I'm going to stick to randomtick though.
//  @Override public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
//    worldIn.getPendingBlockTicks().scheduleTick(pos, this, getTickCooldown(worldIn.rand));
//    super.tick(state, worldIn, pos, rand);
//  }
//
//  @Override public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
//    super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
//    worldIn.getPendingBlockTicks().scheduleTick(pos, this, getTickCooldown(worldIn.rand));
//  }
//
//  private static int getTickCooldown(Random rand) {
//    return 30 + rand.nextInt(10);
//  }
}
