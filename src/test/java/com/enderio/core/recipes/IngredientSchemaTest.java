package com.enderio.core.recipes;

import com.google.gson.JsonElement;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class IngredientSchemaTest extends SchemaTest {
  @Override
  protected String testName() {
    return "ingredient";
  }


  @Test
  void validArray() {
    JSONObject toCheck = getJsonObject("valid_array.json");
    assertThat(validate(toCheck), is(empty()));
  }

  @Test
  void validSingle() {
    JSONObject toCheck = getJsonObject("valid_single.json");
    assertThat(validate(toCheck), is(empty()));
  }

  @Test
  void wrongKeySingle() {
    JSONObject toCheck = getJsonObject("wrong_key_single.json");
    List<String> validationMessages = validate(toCheck);
    assertThat(validationMessages, hasSize(4));
    assertThat(validationMessages, containsInAnyOrder(
            "#/ingredient: required key [Count] not found",
            "#/ingredient: extraneous key [count] is not permitted"
    ));
  }
}
