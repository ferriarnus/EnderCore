package com.enderio.core.common.fluid;

import com.enderio.core.common.util.NullHelper;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import java.lang.reflect.Field;

import net.minecraftforge.fluids.ForgeFlowingFluid.Properties;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public abstract class EnderFlowingFluid extends ForgeFlowingFluid {
  private final boolean flowUpward;

  protected EnderFlowingFluid(Properties properties) {
    super(properties);
    flowUpward = getAttributes().getDensity() < 0;
  }

  // region Upward flow modifications

  @Override protected boolean canBeReplacedWith(FluidState state, BlockGetter world, BlockPos pos, Fluid fluidIn, Direction direction) {
    BlockState bs = NullHelper.notnullF(world, "canDisplace() called without world")
        .getBlockState(NullHelper.notnullF(pos, "canDisplace() called without pos"));
    if (bs.getMaterial().isLiquid()) {
      return false;
    }
    return super.canBeReplacedWith(state, world, pos, fluidIn, direction);
  }

  @Override protected void spread(LevelAccessor worldIn, BlockPos pos, FluidState stateIn) {
    if (!flowUpward) {
      super.spread(worldIn, pos, stateIn);
    } else {
      if (!stateIn.isEmpty()) {
        BlockState blockstate = worldIn.getBlockState(pos);
        BlockPos blockpos = pos.above();
        BlockState blockstate1 = worldIn.getBlockState(blockpos);
        FluidState fluidstate = this.getNewLiquid(worldIn, blockpos, blockstate1);
        if (this.canSpreadTo(worldIn, pos, blockstate, Direction.UP, blockpos, blockstate1, worldIn.getFluidState(blockpos), fluidstate.getType())) {
          this.spreadTo(worldIn, blockpos, blockstate1, Direction.UP, fluidstate);
        }
      }
    }
  }

  // The same as the original, except it allows upward flows.
  @Override public Vec3 getFlow(BlockGetter blockReader, BlockPos pos, FluidState fluidState) {
    double d0 = 0.0D;
    double d1 = 0.0D;
    BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();

    for (Direction direction : Direction.Plane.HORIZONTAL) {
      blockpos$mutable.setWithOffset(pos, direction);
      FluidState fluidstate = blockReader.getFluidState(blockpos$mutable);
      if (this.isSameOrEmpty(fluidstate)) {
        float f = fluidstate.getOwnHeight();
        float f1 = 0.0F;
        if (f == 0.0F) {
          if (!blockReader.getBlockState(blockpos$mutable).getMaterial().blocksMotion()) {
            BlockPos blockpos = blockpos$mutable.below();
            FluidState fluidstate1 = blockReader.getFluidState(blockpos);
            if (this.isSameOrEmpty(fluidstate1)) {
              f = fluidstate1.getOwnHeight();
              if (f > 0.0F) {
                f1 = fluidState.getOwnHeight() - (f - 0.8888889F);
              }
            }
          }
        } else if (f > 0.0F) {
          f1 = fluidState.getOwnHeight() - f;
        }

        if (f1 != 0.0F) {
          d0 += (double) ((float) direction.getStepX() * f1);
          d1 += (double) ((float) direction.getStepZ() * f1);
        }
      }
    }

    Vec3 vector3d = new Vec3(d0, 0.0D, d1);
    if (fluidState.getValue(FALLING)) {
      for (Direction direction1 : Direction.Plane.HORIZONTAL) {
        blockpos$mutable.setWithOffset(pos, direction1);
        if (this.isSolidFace(blockReader, blockpos$mutable, direction1) || this
            .isSolidFace(blockReader, blockpos$mutable.above(), direction1)) {
          vector3d = vector3d.normalize().add(0.0D, flowUpward ? 6.0D : -6.0D, 0.0D);
          break;
        }
      }
    }

    return vector3d.normalize();
  }

  // The same as the original, except it allows upward flows.
  @Override protected FluidState getNewLiquid(LevelReader worldIn, BlockPos pos, BlockState blockStateIn) {
    int i = 0;
    int j = 0;

    for (Direction direction : Direction.Plane.HORIZONTAL) {
      BlockPos blockpos = pos.relative(direction);
      BlockState blockstate = worldIn.getBlockState(blockpos);
      FluidState fluidstate = blockstate.getFluidState();
      if (fluidstate.getType().isSame(this) && this.doesSideHaveHoles(direction, worldIn, pos, blockStateIn, blockpos, blockstate)) {
        if (fluidstate.isSource() && net.minecraftforge.event.ForgeEventFactory
            .canCreateFluidSource(worldIn, blockpos, blockstate, this.canConvertToSource())) {
          ++j;
        }

        i = Math.max(i, fluidstate.getAmount());
      }
    }

    if (j >= 2) {
      BlockState blockstate1 = worldIn.getBlockState(flowUpward ? pos.above() : pos.below());
      FluidState fluidstate1 = blockstate1.getFluidState();
      if (blockstate1.getMaterial().isSolid() || this.isSameAs(fluidstate1)) {
        return this.getSource(false);
      }
    }

    BlockPos blockpos1 = flowUpward ? pos.below() : pos.above();
    BlockState blockstate2 = worldIn.getBlockState(blockpos1);
    FluidState fluidstate2 = blockstate2.getFluidState();
    if (!fluidstate2.isEmpty() && fluidstate2.getType().isSame(this) && this
        .doesSideHaveHoles(flowUpward ? Direction.DOWN : Direction.UP, worldIn, pos, blockStateIn, blockpos1, blockstate2)) {
      return this.getFlowing(8, true);
    } else {
      int k = i - this.getDropOff(worldIn);
      return k <= 0 ? Fluids.EMPTY.defaultFluidState() : this.getFlowing(k, false);
    }
  }

  // Here comes some hateful private copying!!!!

  private static final Field field_212756_e_ref;

  static {
    field_212756_e_ref = ObfuscationReflectionHelper.findField(FlowingFluid.class, "OCCLUSION_CACHE");
  }

  private boolean doesSideHaveHoles(Direction p_212751_1_, BlockGetter p_212751_2_, BlockPos p_212751_3_, BlockState p_212751_4_, BlockPos p_212751_5_,
      BlockState p_212751_6_) {
    Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2bytelinkedopenhashmap;
    if (!p_212751_4_.getBlock().hasDynamicShape() && !p_212751_6_.getBlock().hasDynamicShape()) {
      try {
        object2bytelinkedopenhashmap = ((ThreadLocal<Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>>) field_212756_e_ref.get(this)).get();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
        return false;
      }
    } else {
      object2bytelinkedopenhashmap = null;
    }

    Block.BlockStatePairKey block$rendersidecachekey;
    if (object2bytelinkedopenhashmap != null) {
      block$rendersidecachekey = new Block.BlockStatePairKey(p_212751_4_, p_212751_6_, p_212751_1_);
      byte b0 = object2bytelinkedopenhashmap.getAndMoveToFirst(block$rendersidecachekey);
      if (b0 != 127) {
        return b0 != 0;
      }
    } else {
      block$rendersidecachekey = null;
    }

    VoxelShape voxelshape1 = p_212751_4_.getBlockSupportShape(p_212751_2_, p_212751_3_);
    VoxelShape voxelshape = p_212751_6_.getBlockSupportShape(p_212751_2_, p_212751_5_);
    boolean flag = !Shapes.mergedFaceOccludes(voxelshape1, voxelshape, p_212751_1_);
    if (object2bytelinkedopenhashmap != null) {
      if (object2bytelinkedopenhashmap.size() == 200) {
        object2bytelinkedopenhashmap.removeLastByte();
      }

      object2bytelinkedopenhashmap.putAndMoveToFirst(block$rendersidecachekey, (byte) (flag ? 1 : 0));
    }

    return flag;
  }

  private boolean isSameOrEmpty(FluidState state) {
    return state.isEmpty() || state.getType().isSame(this);
  }

  private boolean isSameAs(FluidState stateIn) {
    return stateIn.getType().isSame(this) && stateIn.isSource();
  }

  // endregion

  // region Flowing and Source

  public static class Flowing extends EnderFlowingFluid {
    public Flowing(Properties properties) {
      super(properties);
      registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7));
    }

    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
      super.createFluidStateDefinition(builder);
      builder.add(LEVEL);
    }

    public int getAmount(FluidState state) {
      return state.getValue(LEVEL);
    }

    public boolean isSource(FluidState state) {
      return false;
    }
  }

  public static class Source extends EnderFlowingFluid {
    public Source(Properties properties) {
      super(properties);
    }

    public int getAmount(FluidState state) {
      return 8;
    }

    public boolean isSource(FluidState state) {
      return true;
    }
  }

  // endregion
}
