package com.enderio.core.recipes;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class EnderItemIngredientSchemaTest extends SchemaTest {

  @Test
  void multiple() {
    JSONObject toCheck = getJsonObject("multiple.json");
    assertThat(validate(toCheck), is(empty()));
  }

  @Test
  void single() {
    JSONObject toCheck = getJsonObject("single.json");
    assertThat(validate(toCheck), is(empty()));
  }

  @Test
  void countTooLarge() {
    JSONObject toCheck = getJsonObject("count_too_large.json");
    List<String> validationMessages = validate(toCheck);
    assertThat(validationMessages, hasSize(1));
    assertThat(validationMessages, contains("#/item_ingredient/count: 65.0 is not lower or equal to 64"));
  }

  @Override
  protected String testName() {
    return "item_ingredient";
  }
}
