package com.enderio.core.recipes;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class FluidStackSchemaTest extends SchemaTest {

  @Test
  void simpleWaterStack() {
    assertThat(validate(getJsonObject("water.json")), is(empty()));
  }

  @Test
  void fluidWithNbt() {
    assertThat(validate(getJsonObject("fluid_with_nbt.json")), is(empty()));
  }

  @Test
  void additionalData() {
    List<String> validationMessages = validate(getJsonObject("additional_data.json"));
    assertThat(validationMessages, hasSize(1));
    assertThat(validationMessages, contains("#/fluid: extraneous key [wrongKey] is not permitted"));
  }

  @Test
  void notEnoughFluid() {
    List<String> validationMessages = validate(getJsonObject("not_enough_fluid.json"));
    assertThat(validationMessages, hasSize(1));
    assertThat(validationMessages, contains("#/fluid/Amount: 0.0 is not higher or equal to 1"));
  }

  @Override
  protected String testName() {
    return "fluidstack";
  }
}
