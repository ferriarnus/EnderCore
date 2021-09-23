package com.enderio.core.client;

import javax.annotation.Nonnull;

import com.enderio.core.client.render.IconUtil;
import com.enderio.core.common.CommonProxy;
import com.enderio.core.common.util.Scheduler;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ClientProxy extends CommonProxy {

  @Override
  public @Nonnull Scheduler getScheduler() {
    if (scheduler != null) {
      return scheduler;
    }
    return scheduler = new Scheduler(false);
  }

  @Override
  public @Nonnull Level getClientWorld() {
    return Minecraft.getInstance().level;
  }

  @Override
  public void throwModCompatibilityError(@Nonnull String... msgs) {
    throw new ModCompatibilityException(msgs);
  }

  @Override
  public void setup(@Nonnull FMLCommonSetupEvent event) {
    IconUtil.instance.init();
  }

}
