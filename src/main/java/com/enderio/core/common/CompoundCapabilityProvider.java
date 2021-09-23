package com.enderio.core.common;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public class CompoundCapabilityProvider implements ICapabilityProvider {

  private final List<ICapabilityProvider> providers = new ArrayList<>();

  public CompoundCapabilityProvider(ICapabilityProvider... provs) {
    if (provs != null) {
      for (ICapabilityProvider p : provs) {
        if (p != null) {
          providers.add(p);
        }
      }
    }
  }

  @Nonnull
  @Override
  public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
    for (ICapabilityProvider prov : providers) {
      LazyOptional<T> optionalCap = prov.getCapability(cap, side);
      if (optionalCap.isPresent())
        return optionalCap;
    }
    return LazyOptional.empty();
  }
}
