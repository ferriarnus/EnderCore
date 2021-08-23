package com.enderio.core.common.recipes;

import com.google.gson.JsonObject;
import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
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
    return validateSchema( SchemaLoader.load(schema), toCheck, new ArrayList<>(), 0, "#");
  }

  /**
   *
   * @param validationSchema Schema to validate agains
   * @param toCheck Object to Check
   * @param errors List of ExceptionMessages to append to
   * @param depth the depth of this exception. Used to put spaces for inner Exceptions, if the JSON violates not the right amount of subschemas. For reference IngredientSchemaTest#wrongKeySingle
   * @param pointerPrefix the previous exception message for calls on parent
   * @return List of ValidationMessages
   */
  private List<String> validateSchema(Schema validationSchema, Object toCheck, List<String> errors, int depth, String pointerPrefix) {
    try {
      validationSchema.validate(toCheck);
    } catch (ValidationException validationException) {
      if (validationException.getCausingExceptions().size() == 0) {
        errors.add(depth(depth) + validationException.getMessage().replaceFirst("#", pointerPrefix));
        if (validationException.getViolatedSchema() instanceof CombinedSchema) {
          errors.add(depth(depth) + "possible options and their violations:");
          String pointer = validationException.getMessage().split(":")[0];
          String[] separatedPointer = pointer.split("/");
          StringBuilder newPointer = new StringBuilder();
          Arrays.stream(separatedPointer).skip(1).forEachOrdered(p -> newPointer.append("/" + p));
          Object violatedObject = findPointerReference(separatedPointer, toCheck);
          ((CombinedSchema)validationException.getViolatedSchema()).getSubschemas().forEach(subschema -> validateSchema(subschema, violatedObject, errors, depth+1, pointerPrefix + newPointer));
        }
        return errors;
      }
      validationException.getCausingExceptions().forEach(cause -> errors.add(depth(depth) + cause.getMessage().replaceFirst("#", pointerPrefix)));
    }
    return errors;
  }

  private String depth(int depth) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < depth; i++) {
      builder.append("  ");
    }
    return builder.toString();
  }

  private Object findPointerReference(String[] pointer, Object toCheck) {
    Object something = toCheck;
    for (String p: pointer) {
      if (p.equals("#"))
        continue;
      if (something instanceof JSONArray) {
        something = ((JSONArray)something).get(Integer.parseInt(p));
        continue;
      }
      if (something instanceof JSONObject) {
        something = ((JSONObject)something).get(p);
      }
    }
    return something;
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
