package com.enderio.core.common.fluid;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.common.util.ITankAccess;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class FluidWrapper {

  @CapabilityInject(IFluidHandler.class)
  private static Capability<IFluidHandler> FLUID_HANDLER = null;

  public static @Nullable IFluidWrapper wrap(BlockGetter world, BlockPos pos, Direction side) {
    if (world == null || pos == null) {
      return null;
    }
    return wrap(world.getBlockEntity(pos), side);
  }

  public static @Nullable IFluidWrapper wrap(@Nullable BlockEntity te, Direction side) {
    if (te != null && te.hasLevel() && !te.isRemoved()) {
      if (te instanceof IFluidWrapper) {
        return (IFluidWrapper) te;
      }
      final Capability<IFluidHandler> fluidHandler = FLUID_HANDLER;
      if (fluidHandler == null) {
        throw new NullPointerException("Capability<IFluidHandler> missing");
      }
      LazyOptional<IFluidHandler> cap = te.getCapability(fluidHandler, side);
      return wrap(cap.resolve().orElse(null));
    }
    return null;
  }

  public static TankAccessFluidWrapper wrap(final ITankAccess tankAccess) {
    return new TankAccessFluidWrapper(tankAccess);
  }

  public static CapabilityFluidWrapper wrap(final IFluidHandler fluidHandler) {
    if (fluidHandler != null) {
      return new CapabilityFluidWrapper(fluidHandler);
    }
    return null;
  }

  public static @Nullable IFluidWrapper wrap(@Nullable IFluidWrapper wrapper) {
    return wrapper;
  }

  public static @Nullable IFluidWrapper wrap(@Nonnull ItemStack itemStack) {
    return wrap(FluidUtil.getFluidHandler(itemStack).resolve().orElse(null));
  }

  public static Map<Direction, IFluidWrapper> wrapNeighbours(LevelReader world, BlockPos pos) {
    Map<Direction, IFluidWrapper> res = new EnumMap<Direction, IFluidWrapper>(Direction.class);
    for (Direction dir : Direction.values()) {
      if (dir == null) {
        throw new NullPointerException("Direction.values() contains null values???");
      }
      IFluidWrapper wrapper = wrap(world, pos.relative(dir), dir.getOpposite());
      if (wrapper != null) {
        res.put(dir, wrapper);
      }
    }
    return res;
  }

  // Some helpers:

  public static int transfer(FluidTank from, FluidTank to, int limit) {
    return transfer(wrap(from), wrap(to), limit);
  }

  public static int transfer(FluidTank from, IFluidWrapper to, int limit) {
    return transfer(wrap(from), wrap(to), limit);
  }

  public static int transfer(FluidTank from, BlockGetter world, BlockPos topos, Direction toDirection, int limit) {
    return transfer(wrap(from), wrap(world, topos, toDirection), limit);
  }

  public static int transfer(FluidTank from, BlockEntity to, Direction toDirection, int limit) {
    return transfer(wrap(from), wrap(to, toDirection), limit);
  }

  //

  public static int transfer(IFluidWrapper from, FluidTank to, int limit) {
    return transfer(wrap(from), wrap(to), limit);
  }

  public static int transfer(IFluidWrapper from, BlockGetter world, BlockPos topos, Direction toDirection, int limit) {
    return transfer(wrap(from), wrap(world, topos, toDirection), limit);
  }

  public static int transfer(IFluidWrapper from, BlockEntity to, Direction toDirection, int limit) {
    return transfer(wrap(from), wrap(to, toDirection), limit);
  }

  //

  public static int transfer(BlockGetter world, BlockPos frompos, Direction fromDirection, IFluidWrapper to, int limit) {
    return transfer(wrap(world, frompos, fromDirection), wrap(to), limit);
  }

  public static int transfer(BlockGetter world, BlockPos frompos, Direction fromDirection, FluidTank to, int limit) {
    return transfer(wrap(world, frompos, fromDirection), wrap(to), limit);
  }

  public static int transfer(BlockGetter world, BlockPos frompos, Direction fromDirection, BlockPos topos, Direction toDirection, int limit) {
    return transfer(wrap(world, frompos, fromDirection), wrap(world, topos, toDirection), limit);
  }

  public static int transfer(BlockGetter world, BlockPos frompos, Direction fromDirection, BlockEntity to, Direction toDirection, int limit) {
    return transfer(wrap(world, frompos, fromDirection), wrap(to, toDirection), limit);
  }

  //

  public static int transfer(BlockEntity from, Direction fromDirection, IFluidWrapper to, int limit) {
    return transfer(wrap(from, fromDirection), wrap(to), limit);
  }

  public static int transfer(BlockEntity from, Direction fromDirection, FluidTank to, int limit) {
    return transfer(wrap(from, fromDirection), wrap(to), limit);
  }

  public static int transfer(BlockEntity from, Direction fromDirection, BlockGetter world, BlockPos topos, Direction toDirection, int limit) {
    return transfer(wrap(from, fromDirection), wrap(world, topos, toDirection), limit);
  }

  public static int transfer(BlockEntity from, Direction fromDirection, BlockEntity to, Direction toDirection, int limit) {
    return transfer(wrap(from, fromDirection), wrap(to, toDirection), limit);
  }

  //

  public static int transfer(IFluidWrapper from, IFluidWrapper to, int limit) {
    if (from == null || to == null || limit <= 0) {
      return 0;
    }

    FluidStack drainable = from.getAvailableFluid();
    if (drainable == null || drainable.getAmount() <= 0) {
      return 0;
    }
    drainable = drainable.copy();

    if (drainable.getAmount() > limit) {
      drainable.setAmount(limit);
    }

    int fillable = to.offer(drainable);
    if (fillable <= 0 || fillable > drainable.getAmount()) {
      return 0;
    }

    if (fillable < drainable.getAmount()) {
      drainable.setAmount(fillable);
    }

    return to.fill(from.drain(drainable));
  }

}
