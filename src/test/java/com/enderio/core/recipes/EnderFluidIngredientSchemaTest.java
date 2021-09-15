package com.enderio.core.recipes;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class EnderFluidIngredientSchemaTest extends SchemaTest{

  @Test
  void fluidTag() {
    JSONObject toCheck = getJsonObject("fluid_tag.json");
    assertThat(validate(toCheck), is(empty()));
  }

  @Test
  void multipleFluids() {
    JSONObject toCheck = getJsonObject("multiple_fluids.json");
    assertThat(validate(toCheck), is(empty()));
  }

  @Test
  void wrongAmount() {
    JSONObject toCheck = getJsonObject("wrong_amount.json");
    List<String> validationMessages = validate(toCheck);
    assertThat(validationMessages, hasSize(1));
    assertThat(validationMessages, contains("#/ingredient/amount: 0.0 is not higher or equal to 1"));
  }

  @Override
  protected String testName() {
    return "fluid_ingredient";
  }
}
