package com.enderio.core.recipes;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

 class RegistryNameSchemaTest extends SchemaTest {
  @Override
  protected String testName() {
    return "registryname";
  }

  @Test
  void valid() {
    JSONObject toCheck = getJsonObject("valid.json");
    assertThat(validate(toCheck), is(empty()));
  }

  @Test
  void slashes() {
    JSONObject toCheck = getJsonObject("slashes.json");
    assertThat(validate(toCheck), is(empty()));
  }

  @Test
  void uppercase() {
    JSONObject toCheck = getJsonObject("uppercase.json");
    List<String> validationMessages = validate(toCheck);
    assertThat(validationMessages, hasSize(1));
    assertThat(validationMessages, contains("#/id: string [Minecraft:some_id] does not match pattern ^[a-z0-9_]+:[a-z0-9_/]+$"));
  }

  @Test
  void onlyOnePart() {
    JSONObject toCheck = getJsonObject("only_one_part.json");
    List<String> validationMessages = validate(toCheck);
    assertThat(validationMessages, hasSize(1));
    assertThat(validationMessages, contains("#/id: string [minecraftsome_id] does not match pattern ^[a-z0-9_]+:[a-z0-9_/]+$"));
  }

  @Test
  void noFirstPart() {
    JSONObject toCheck = getJsonObject("no_first_part.json");
    List<String> validationMessages = validate(toCheck);
    assertThat(validationMessages, hasSize(1));
    assertThat(validationMessages, contains("#/id: string [:some_id] does not match pattern ^[a-z0-9_]+:[a-z0-9_/]+$"));
  }
}
