package com.enderio.core.common.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Stream;

import net.minecraft.world.item.crafting.Ingredient.Value;
import net.minecraft.world.item.crafting.Ingredient.ItemValue;

public class EnderItemIngredient extends Ingredient {
  private int count;

  /**
   * For serialization purposes, so we only need to serialize count and the parent
   */
  @Nonnull
  private Ingredient parent;


  public EnderItemIngredient(Stream<? extends Value> itemLists, int count) {
    super(itemLists);
    parent = Ingredient.fromValues(itemLists);
    this.count = count;
  }
  public EnderItemIngredient(Ingredient ingredient, int count) {
    super(Arrays.stream(ingredient.getItems()).map(ItemValue::new));
    this.parent = ingredient;
    this.count = count;
  }


  @Override
  public ItemStack[] getItems() {
    ItemStack[] matchingStacks = super.getItems();
    for (ItemStack matchingStack : matchingStacks) {
      matchingStack.setCount(count);
    }
    return matchingStacks;
  }

  @Override
  public JsonElement toJson() {
    JsonObject json = new JsonObject();
    json.add("items", parent.toJson());
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
      Ingredient parent = Ingredient.fromJson(json.get("items"));
      int count = json.get("count").getAsInt();
      return new EnderItemIngredient(parent, count);
    }

    @Override
    public EnderItemIngredient parse(FriendlyByteBuf buffer) {
      Ingredient ingredient = Ingredient.fromNetwork(buffer);
      return new EnderItemIngredient(ingredient, buffer.readShort());
    }

    @Override
    public void write(FriendlyByteBuf buffer, EnderItemIngredient ingredient) {
      ingredient.parent.toNetwork(buffer);
      buffer.writeShort(ingredient.count);
    }
  }
}
