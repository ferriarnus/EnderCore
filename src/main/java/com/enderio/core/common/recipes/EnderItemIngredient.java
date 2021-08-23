package com.enderio.core.common.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.NBTIngredient;
import org.json.JSONObject;
import org.lwjgl.system.CallbackI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Stream;

public class EnderItemIngredient extends Ingredient {
  private int count;

  /**
   * For serialization purposes, so we only need to serialize count and the parent
   */
  @Nonnull
  private Ingredient parent;


  public EnderItemIngredient(Stream<? extends IItemList> itemLists, int count) {
    super(itemLists);
    parent = Ingredient.fromItemListStream(itemLists);
    this.count = count;
  }
  public EnderItemIngredient(Ingredient ingredient, int count) {
    super(Arrays.stream(ingredient.getMatchingStacks()).map(SingleItemList::new));
    this.parent = ingredient;
    this.count = count;
  }


  @Override
  public ItemStack[] getMatchingStacks() {
    ItemStack[] matchingStacks = super.getMatchingStacks();
    for (ItemStack matchingStack : matchingStacks) {
      matchingStack.setCount(count);
    }
    return matchingStacks;
  }

  @Override
  public JsonElement serialize() {
    JsonObject json = new JsonObject();
    json.add("items", parent.serialize());
    json.addProperty("count", count);
    return json;
  }

  @Override
  public boolean test(@Nullable ItemStack itemStack) {
    return super.test(itemStack) && itemStack.getCount() >= count;
  }

  @Override
  public IIngredientSerializer<EnderItemIngredient> getSerializer() {
    return Serializer.INSTANCE;
  }

  public static class Serializer implements IIngredientSerializer<EnderItemIngredient> {
    public static final Serializer INSTANCE = new Serializer();
    private Serializer() {
    }

    @Override
    public EnderItemIngredient parse(JsonObject json) {
      Ingredient parent = Ingredient.deserialize(json.get("items"));
      int count = json.get("count").getAsInt();
      return new EnderItemIngredient(parent, count);
    }

    @Override
    public EnderItemIngredient parse(PacketBuffer buffer) {
      Ingredient ingredient = Ingredient.read(buffer);
      return new EnderItemIngredient(ingredient, buffer.readShort());
    }

    @Override
    public void write(PacketBuffer buffer, EnderItemIngredient ingredient) {
      ingredient.parent.write(buffer);
      buffer.writeShort(ingredient.count);
    }
  }
}
