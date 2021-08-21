package com.enderio.core.recipes;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ItemStackSchemaTest extends SchemaTest {

  JSONObject schema;

  @BeforeAll
  void createSchema() {
    schema = getJsonObject("single_itemstack.json");
  }

  @Test
  void validItemStack() {
    JSONObject toCheck = getJsonObject("valid.json");
    Assertions.assertSame(0, validateItemStack(toCheck).size());
  }

  @Test
  void validWithNBT() {
    JSONObject toCheck = getJsonObject("valid_with_nbt.json");
    Assertions.assertSame(0, validateItemStack(toCheck).size());
  }

  @Test
  void countLarge() {
    JSONObject toCheck = getJsonObject("count_large.json");
    Assertions.assertSame(0, validateItemStack(toCheck).size());
  }

  @Test
  void countTooLarge() {
    JSONObject toCheck = getJsonObject("count_too_large.json");
    List<String> validationMessages = validateItemStack(toCheck);
    Assertions.assertSame(1, validationMessages.size());
    Assertions.assertEquals("#/output/Count: 65.0 is not lower or equal to 64", validationMessages.get(0));
  }

  @Test
  void countSmall() {
    JSONObject toCheck = getJsonObject("count_small.json");
    Assertions.assertSame(0, validateItemStack(toCheck).size());
  }

  @Test
  void countTooSmall() {
    JSONObject toCheck = getJsonObject("count_too_small.json");
    List<String> validationMessages = validateItemStack(toCheck);
    Assertions.assertSame(1, validationMessages.size());
    Assertions.assertEquals("#/output/Count: 0.0 is not higher or equal to 1", validationMessages.get(0));
  }

  @Test
  void invalidType() {
    JSONObject toCheck = getJsonObject("invalid_number_type.json");
    List<String> validationMessages = validateItemStack(toCheck);
    Assertions.assertSame(1, validationMessages.size());
    Assertions.assertEquals("#/output/Count: expected type: Integer, found: Double", validationMessages.get(0));
  }

  @Test
  void wrongName() {
    JSONObject toCheck = getJsonObject("wrong_name.json");
    List<String> validationMessages = validateItemStack(toCheck);
    Assertions.assertSame(2, validationMessages.size());
    Assertions.assertEquals("#/output: required key [Count] not found", validationMessages.get(0));
    Assertions.assertEquals("#/output: extraneous key [count] is not permitted", validationMessages.get(1));
  }

  @Test
  void validateRealItemStacks() {
    validateRealItemStack(new ItemStack(Items.DIAMOND));
    validateRealItemStack(new ItemStack(Items.DIAMOND,1));
    validateRealItemStack(new ItemStack(Items.ANVIL,64));
  }

  private void validateRealItemStack(ItemStack stack) {
    JsonElement json = ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, stack).result().orElseThrow(
            () -> new IllegalStateException("Could not encode itemStack"));
    JSONObject object = new JSONObject();
    object.put("output", new JSONObject(json.toString()));
    Assertions.assertSame(0, validateItemStack(object).size());
  }

  private List<String> validateItemStack(JSONObject toCheck) {
    return validateSchema(schema, toCheck);
  }
  @Override
  protected String testName() {
    return "itemstack";
  }
}
