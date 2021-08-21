package com.enderio.core.recipes;

import com.enderio.core.common.recipes.SchemaHelper;
import com.google.gson.JsonElement;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class SchemaTest {

  JSONObject schema;

  @BeforeAll
  void createSchema() {
    schema = getJsonObject("schema.json");
  }

  protected abstract String testName();

  protected JSONObject getJsonObject(@Nonnull String file) {
    return new JSONObject(new JSONTokener(getFileStream(file)));
  }

  @Nonnull
  protected InputStream getFileStream(@Nonnull String file) {
    return Objects.requireNonNull(getClass().getResourceAsStream("/recipes/schema/" + testName() + "/" + file));
  }

  protected List<String> validate(JSONObject toCheck) {
    return validateSchema(schema, toCheck);
  }

  protected List<String> validateSchema(JSONObject jsonSchema, JSONObject toCheck) {
    return SchemaHelper.getInstance().validateSchema(SchemaHelper.getInstance().insertDefinition(jsonSchema), toCheck);
  }

  protected JSONObject wrapJsonElement(JsonElement json) {
    return new JSONObject(json.toString());
  }
}
