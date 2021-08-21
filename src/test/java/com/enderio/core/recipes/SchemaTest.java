package com.enderio.core.recipes;

import com.enderio.core.common.recipes.SchemaHelper;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public abstract class SchemaTest {

  protected abstract String testName();

  protected JSONObject getJsonObject(@Nonnull String file) {
    return new JSONObject(new JSONTokener(getFileStream(file)));
  }

  @Nonnull
  protected InputStream getFileStream(@Nonnull String file) {
    return Objects.requireNonNull(getClass().getResourceAsStream("/recipes/schema/" + testName() + "/" + file));
  }

  protected List<String> validateSchema(JSONObject jsonSchema, JSONObject toCheck) {
    return SchemaHelper.getInstance().validateSchema(SchemaHelper.getInstance().insertDefinition(jsonSchema), toCheck);
  }
}
