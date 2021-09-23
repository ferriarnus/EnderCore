package com.enderio.core.common.util;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;

public class SoundUtil {

  public static void playClientSoundFX(@Nonnull SoundEvent name, @Nonnull BlockEntity te) {
    Level world = Minecraft.getInstance().level;
    world.playLocalSound(te.getBlockPos().getX() + 0.5, te.getBlockPos().getY() + 0.5, te.getBlockPos().getZ() + 0.5, name, SoundSource.BLOCKS, 0.1F,
        0.5F * ((world.random.nextFloat() - world.random.nextFloat()) * 0.7F + 1.8F), true);
  }
}
