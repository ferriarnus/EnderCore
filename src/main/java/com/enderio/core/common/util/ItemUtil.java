package com.enderio.core.common.util;

import javax.annotation.Nonnull;

import com.enderio.core.EnderCore;
import com.enderio.core.common.vecmath.Vec3f;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class ItemUtil {

  public static void spawnItemInWorldWithRandomMotion(@Nonnull Level world, @Nonnull ItemStack item, @Nonnull BlockPos pos) {
    spawnItemInWorldWithRandomMotion(world, item, pos.getX(), pos.getY(), pos.getZ());
  }

  /**
   * Spawns an ItemStack into the world with motion that simulates a normal block drop.
   *
   * @param world
   *          The world object.
   * @param item
   *          The ItemStack to spawn.
   * @param x
   *          X coordinate of the block in which to spawn the entity.
   * @param y
   *          Y coordinate of the block in which to spawn the entity.
   * @param z
   *          Z coordinate of the block in which to spawn the entity.
   */
  public static void spawnItemInWorldWithRandomMotion(@Nonnull Level world, @Nonnull ItemStack item, int x, int y, int z) {
    if (!item.isEmpty()) {
      spawnItemInWorldWithRandomMotion(new ItemEntity(world, x + 0.5, y + 0.5, z + 0.5, item));
    }
  }

  /**
   * Spawns an ItemStack into the world with motion that simulates a normal block drop.
   *
   * @param world
   *          The world object.
   * @param item
   *          The ItemStack to spawn.
   * @param pos
   *          The block location to spawn the item.
   * @param hitX
   *          The location within the block to spawn the item at.
   * @param hitY
   *          The location within the block to spawn the item at.
   * @param hitZ
   *          The location within the block to spawn the item at.
   * @param scale
   *          The factor with which to push the spawn location out.
   */
  public static void spawnItemInWorldWithRandomMotion(@Nonnull Level world, @Nonnull ItemStack item, @Nonnull BlockPos pos, float hitX, float hitY, float hitZ,
      float scale) {
    Vec3f v = new Vec3f((hitX - .5f), (hitY - .5f), (hitZ - .5f));
    v.normalize();
    v.scale(scale);
    float x = pos.getX() + .5f + v.x;
    float y = pos.getY() + .5f + v.y;
    float z = pos.getZ() + .5f + v.z;
    spawnItemInWorldWithRandomMotion(new ItemEntity(world, x, y, z, item));
  }

  /**
   * Spawns an EntityItem into the world with motion that simulates a normal block drop.
   *
   * @param entity
   *          The entity to spawn.
   */
  public static void spawnItemInWorldWithRandomMotion(@Nonnull ItemEntity entity) {
    entity.setDefaultPickUpDelay();
    entity.level.addFreshEntity(entity);
  }


  public static String getDurabilityString(@Nonnull ItemStack item) {
    if (item.isEmpty()) {
      return null;
    }
    return EnderCore.lang.localize("tooltip.durability") + " " + (item.getMaxDamage() - item.getDamageValue()) + "/" + item.getMaxDamage();
  }

  /**
   * Gets an NBT tag from an ItemStack, creating it if needed. The tag returned will always be the same as the one on the stack.
   *
   * @param stack
   *          The ItemStack to get the tag from.
   * @return An NBTTagCompound from the stack.
   */
  public static CompoundTag getOrCreateNBT(ItemStack stack) {
    if (!stack.hasTag()) {
      stack.setTag(new CompoundTag());
    }
    return stack.getTag();
  }

  public static boolean isStackFull(@Nonnull ItemStack contents) {
    return contents.getCount() >= contents.getMaxStackSize();
  }

  /**
   * Checks if items, damage and NBT are equal and the items are stackable.
   *
   * @param s1
   * @param s2
   * @return True if the two stacks are mergeable, false otherwise.
   */
  public static boolean areStackMergable(@Nonnull ItemStack s1, @Nonnull ItemStack s2) {
    if (s1.isEmpty() || s2.isEmpty() || !s1.isStackable() || !s2.isStackable()) {
      return false;
    }
    if (!s1.sameItem(s2)) {
      return false;
    }
    return ItemStack.tagMatches(s1, s2);
  }

  /**
   * Checks if items, damage and NBT are equal.
   *
   * @param s1
   * @param s2
   * @return True if the two stacks are equal, false otherwise.
   */
  public static boolean areStacksEqual(@Nonnull ItemStack s1, @Nonnull ItemStack s2) {
    if (s1.isEmpty() || s2.isEmpty()) {
      return false;
    }
    if (!s1.sameItem(s2)) {
      return false;
    }
    return ItemStack.tagMatches(s1, s2);
  }

  /**
   * Checks if items, meta and NBT are equal.
   *
   * @param s1
   * @param s2
   * @return True if the two stacks are equal, false otherwise.
   */
  public static boolean areStacksEqualIgnoringDamage(@Nonnull ItemStack s1, @Nonnull ItemStack s2) {
    if (s1.isEmpty() || s2.isEmpty()) {
      return false;
    }
    if (!s1.sameItemStackIgnoreDurability(s2)) {
      return false;
    }
    return ItemStack.tagMatches(s1, s2);
  }

  /**
   * Tries to put an item into the player's inventory as if it was picked up in the world.
   * <p>
   * Note: Needs to be called server-side.
   * 
   * @param player
   *          The player
   * @param itemstack
   *          The item to pick up
   * @return The remaining stack. Empty when all was picked up.
   */
  public static @Nonnull ItemStack fakeItemPickup(@Nonnull Player player, @Nonnull ItemStack itemstack) {
    if (!player.level.isClientSide) {
      ItemEntity entityItem = new ItemEntity(player.level, player.getX(), player.getY(), player.getZ(), itemstack);
      entityItem.playerTouch(player);
      if (!entityItem.isAlive()) {
        return ItemStack.EMPTY;
      } else {
        entityItem.setAirSupply(1);
        return entityItem.getItem();
      }
    }
    return itemstack;
  }

}
