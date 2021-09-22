package com.enderio.core.common;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public class MappedCapabilityProvider implements ICapabilityProvider {

  private final Map<Capability<?>, Object> providers = new HashMap<>();

  public @Nonnull <T> MappedCapabilityProvider add(@Nullable Capability<T> capability, @Nonnull T cap) {
    providers.put(capability, cap);
    return this;
  }

  @Nonnull
  @Override
  public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
    return LazyOptional.of(() -> providers.get(cap)).cast();
  }

}
