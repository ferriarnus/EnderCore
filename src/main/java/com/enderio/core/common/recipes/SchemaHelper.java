package com.enderio.core.common.recipes;

import com.google.gson.JsonObject;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SchemaHelper {
  private static SchemaHelper instance;
  String definitionLocation = "file://" + getClass().getClassLoader().getResource("assets/endercore/recipes/schema/definitions.json").getPath();
  private SchemaHelper() {}

  public static SchemaHelper getInstance() {
    if (instance == null)
      instance = new SchemaHelper();
    return instance;
  }
  public List<String> validateSchema(JSONObject schema, JSONObject toCheck) {
    Schema validationSchema = SchemaLoader.load(schema);
    List<String> errors = new ArrayList<>();
    try {
      validationSchema.validate(toCheck);
    } catch (ValidationException validationException) {
      if (validationException.getCausingExceptions().size() == 0) {
        errors.add(validationException.getMessage());
        return errors;
      }
      validationException.getCausingExceptions().forEach(cause -> errors.add(cause.getMessage()));
    }
    return errors;
  }
  public JSONObject insertDefinition(JSONObject json) {
    search("", json, empty -> {});
    return json;
  }
  private void search(String key, Object json, Consumer<String> replaceWith) {
    if (json instanceof JSONObject) {
      JSONObject jsonObject = (JSONObject) json;
      jsonObject.keys().forEachRemaining(innerKeys -> {
        search(innerKeys, jsonObject.get(innerKeys), replaced -> jsonObject.put(innerKeys, replaced));
      });
    }
    if (json instanceof JSONArray) {
      JSONArray array = (JSONArray) json;
      for (int i = 0; i < array.length(); i++) {
          int finalI = i;
          search("", array.get(i), replaced -> array.put(finalI, replaced));
      }
    }
    if (json instanceof String) {
      if (key.equals("$ref") && ((String) json).startsWith("${EnderCoreDefinitions}")) {
        replaceWith.accept( definitionLocation + ((String) json).substring(23));
      }
    }
  }
}
