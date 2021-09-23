package com.enderio.core.recipes;

import com.mojang.serialization.JsonOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ItemStackSchemaTest extends SchemaTest {

  @Test
  void validItemStack() {
    JSONObject toCheck = getJsonObject("valid.json");
    assertThat(validate(toCheck), is(empty()));
  }

  @Test
  void validWithNBT() {
    JSONObject toCheck = getJsonObject("valid_with_nbt.json");
    assertThat(validate(toCheck), is(empty()));
  }

  @Test
  void countLarge() {
    JSONObject toCheck = getJsonObject("count_large.json");
    assertThat(validate(toCheck), is(empty()));
  }

  @Test
  void countTooLarge() {
    JSONObject toCheck = getJsonObject("count_too_large.json");
    List<String> validationMessages = validate(toCheck);
    assertThat(validationMessages, hasSize(1));
    assertThat(validationMessages, contains("#/output/Count: 65.0 is not lower or equal to 64"));
  }

  @Test
  void countSmall() {
    JSONObject toCheck = getJsonObject("count_small.json");
    assertThat(validate(toCheck), is(empty()));
  }

  @Test
  void countTooSmall() {
    JSONObject toCheck = getJsonObject("count_too_small.json");
    List<String> validationMessages = validate(toCheck);
    assertThat(validationMessages, hasSize(1));
    assertThat(validationMessages, contains("#/output/Count: 0.0 is not higher or equal to 1"));
  }

  @Test
  void invalidType() {
    JSONObject toCheck = getJsonObject("invalid_number_type.json");
    List<String> validationMessages = validate(toCheck);
    assertThat(validationMessages, hasSize(1));
    assertThat(validationMessages, contains("#/output/Count: expected type: Integer, found: Double"));
  }

  @Test
  void wrongName() {
    JSONObject toCheck = getJsonObject("wrong_name.json");
    List<String> validationMessages = validate(toCheck);
    assertThat(validationMessages, hasSize(2));
    assertThat(validationMessages, containsInAnyOrder(
            "#/output: required key [Count] not found",
            "#/output: extraneous key [count] is not permitted"
    ));
  }

  @Test
  void wrongRegistryName() {
    JSONObject toCheck = getJsonObject("wrong_registryname.json");
    List<String> validationMessages = validate(toCheck);
    assertThat(validationMessages, hasSize(1));
    assertThat(validationMessages, contains("#/output/id: string [minecraft_something] does not match pattern ^[a-z0-9_]+:[a-z0-9_/]+$"));
  }

  @Test
  void validateRealItemStacks() {
    validateRealItemStack(new ItemStack(Items.DIAMOND));
    validateRealItemStack(new ItemStack(Items.DIAMOND,1));
    validateRealItemStack(new ItemStack(Items.ANVIL,64));
    assertThat(parseAndValidateItemStack(new ItemStack(Items.ACACIA_LEAVES, 65)), hasSize(1));
    assertThat(parseAndValidateItemStack(new ItemStack(Items.ACACIA_LEAVES, 0)), hasSize(1));
  }


  private void validateRealItemStack(ItemStack stack) {
    assertThat(parseAndValidateItemStack(stack), is(empty()));
  }

  private List<String> parseAndValidateItemStack(ItemStack stack) {
    JSONObject json = wrapJsonElement(ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, stack).result().orElseThrow(
            () -> new IllegalStateException("Could not encode itemStack")));
    JSONObject toCheck = new JSONObject();
    toCheck.put("output", json);
    return validate(toCheck);
  }

  @Override
  protected String testName() {
    return "itemstack";
  }
}
