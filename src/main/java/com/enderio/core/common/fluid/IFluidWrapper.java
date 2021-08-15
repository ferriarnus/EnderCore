package com.enderio.core.common.fluid;

import javax.annotation.Nullable;

import net.minecraft.util.Tuple;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public interface IFluidWrapper {
  int offer(FluidStack resource);

  int fill(FluidStack resource);

  @Nullable FluidStack drain(FluidStack resource);

  @Nullable FluidStack getAvailableFluid();

  List<Tuple<FluidStack, Integer>> getFluidInTanks();
}
