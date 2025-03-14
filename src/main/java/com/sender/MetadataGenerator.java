package com.sender;

import com.google.gson.*;

public class MetadataGenerator {

    public String generateMetadataJson(JsonObject config) {
        JsonObject meta = new JsonObject();
        meta.addProperty("meta", true);
        meta.addProperty("cellCount", config.get("cellCount").getAsInt());

        JsonArray fields = new JsonArray();

        for (JsonElement el : config.getAsJsonArray("fields")) {
            JsonObject field = el.getAsJsonObject();
            JsonObject fieldMeta = new JsonObject();

            fieldMeta.addProperty("key", field.get("key").getAsString());
            fieldMeta.addProperty("unit", field.get("unit").getAsString());

            fields.add(fieldMeta);
        }

        meta.add("fields", fields);
        return new GsonBuilder().setPrettyPrinting().create().toJson(meta);
    }
}
