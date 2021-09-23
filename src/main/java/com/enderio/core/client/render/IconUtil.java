package com.enderio.core.client.render;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import com.enderio.core.EnderCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class IconUtil {

  public interface IIconProvider {

    void registerIcons(@Nonnull TextureAtlas register);

  }

  public static final @Nonnull IconUtil instance = new IconUtil();

  public static void addIconProvider(@Nonnull IIconProvider registrar) {
    instance.iconProviders.add(registrar);
  }

  private final @Nonnull ArrayList<IIconProvider> iconProviders = new ArrayList<>();

  public @Nonnull TextureAtlasSprite whiteTexture;
  public @Nonnull TextureAtlasSprite blankTexture;
  public @Nonnull TextureAtlasSprite errorTexture;

  private boolean doneInit = false;

  @SuppressWarnings("null")
  private IconUtil() {
  }

  public void init() {
    if (doneInit) {
      return;
    }
    doneInit = true;
    MinecraftForge.EVENT_BUS.register(this);
    addIconProvider(register -> {
      whiteTexture = register.getSprite(new ResourceLocation(EnderCore.MODID, "white"));
      errorTexture = register.getSprite(new ResourceLocation(EnderCore.MODID, "error"));
      blankTexture = register.getSprite(new ResourceLocation(EnderCore.MODID, "blank"));
    });
  }

  @SubscribeEvent
  public void onIconLoad(TextureStitchEvent.Pre event) {
    for (IIconProvider reg : iconProviders) {
      final TextureAtlas map = event.getMap();
      if (map != null) {
        reg.registerIcons(map);
      }
    }
  }

  @SuppressWarnings("null") // don't trust modded models to not do stupid things...
  public static @Nonnull TextureAtlasSprite getIconForItem(@Nonnull ItemStack itemStack) {
    final TextureAtlasSprite icon = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getParticleIcon(itemStack);
    return icon != null ? icon : Minecraft.getInstance().getTextureAtlas(new ResourceLocation("minecraft", "textures/atlas/blocks.png")).apply(MissingTextureAtlasSprite.getLocation());
  }

}