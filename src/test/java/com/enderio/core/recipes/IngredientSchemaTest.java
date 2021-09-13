package com.enderio.core.recipes;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
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
    assertThat(validationMessages, hasSize(10));
    assertThat(validationMessages, contains(
            "#/ingredient: #: 0 subschemas matched instead of one",
            "possible options and their violations:",
            "  #/ingredient: expected type: JSONArray, found: JSONObject",
            "  #/ingredient: #: only 0 subschema matches out of 2",
            "  possible options and their violations:",
            "    #/ingredient: extraneous key [tags] is not permitted",
            "    #/ingredient: #: 0 subschemas matched instead of one",
            "    possible options and their violations:",
            "      #/ingredient: required key [item] not found",
            "      #/ingredient: required key [tag] not found"
    ));
  }

  @Test
  void emptyArray() {
    JSONObject toCheck = getJsonObject("empty_array.json");
    List<String> validationMessages = validate(toCheck);
    assertThat(validationMessages, hasSize(8));
    assertThat(validationMessages, contains(
            "#/ingredient: #: 0 subschemas matched instead of one",
            "possible options and their violations:",
            "  #/ingredient: expected minimum item count: 1, found: 0",
            "  #/ingredient: #: only 0 subschema matches out of 2",
            "  possible options and their violations:",
            "    #/ingredient: expected type: JSONObject, found: JSONArray",
            "    #/ingredient: #: 2 subschemas matched instead of one",
            "    possible options and their violations:"
    ));
  }
}
