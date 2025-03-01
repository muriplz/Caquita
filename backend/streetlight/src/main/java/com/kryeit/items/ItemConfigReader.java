package com.kryeit.items;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kryeit.recycling.ResourceType;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemConfigReader {
    private static final List<ConfigItem> ALL_ITEMS = new ArrayList<>();
    private static boolean initialized = false;

    public ItemConfigReader() {
        if (!initialized) {
            loadAllItems();
            System.out.println(ALL_ITEMS);
            initialized = true;
        }
    }

    public static List<ConfigItem> getAllItems() {
        if (!initialized) {
            new ItemConfigReader();
        }
        return ALL_ITEMS;
    }

    public static List<ConfigItem> getItemsByType(ResourceType type) {
        if (!initialized) {
            new ItemConfigReader();
        }
        return ALL_ITEMS.stream()
                .filter(item -> item.resourceType() == type)
                .collect(Collectors.toList());
    }

    public static ConfigItem getItemById(String id) {
        if (!initialized) {
            new ItemConfigReader();
        }
        return ALL_ITEMS.stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    private void loadAllItems() {
        Path resourcesPath = findResourcesPath();

        if (!Files.exists(resourcesPath)) {
            throw new RuntimeException("Resources directory not found at: " + resourcesPath.toAbsolutePath());
        }

        Path dataItemsPath = resourcesPath.resolve("data").resolve("items");
        if (Files.exists(dataItemsPath) && Files.isDirectory(dataItemsPath)) {
            resourcesPath = dataItemsPath;
        }

        try {
            File[] typeDirs = resourcesPath.toFile().listFiles(File::isDirectory);

            for (File typeDir : typeDirs) {
                ResourceType resourceType = ResourceType.fromFolderName(typeDir.getName());

                File[] jsonFiles = typeDir.listFiles(file -> file.isFile() && file.getName().endsWith(".json"));
                if (jsonFiles == null) {
                    continue;
                }

                for (File jsonFile : jsonFiles) {
                    try {
                        loadItemsFromFile(jsonFile, resourceType);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Path findResourcesPath() {
        return Paths.get("src", "main", "resources");
    }

    private void loadItemsFromFile(File jsonFile, ResourceType resourceType) throws IOException {
        try (FileReader reader = new FileReader(jsonFile)) {
            JsonArray itemsArray = JsonParser.parseReader(reader).getAsJsonArray();

            for (JsonElement element : itemsArray) {
                JsonObject itemObj = element.getAsJsonObject();

                if (!itemObj.has("id")) {
                    continue;
                }

                String id = itemObj.get("id").getAsString();
                String type = itemObj.has("type") ? itemObj.get("type").getAsString() : "";
                String rarity = itemObj.has("rarity") ? itemObj.get("rarity").getAsString() : "";

                List<String> right = new ArrayList<>();
                if (itemObj.has("right") && itemObj.get("right").isJsonArray()) {
                    JsonArray rightArray = itemObj.getAsJsonArray("right");
                    for (JsonElement rightElement : rightArray) {
                        right.add(rightElement.getAsString());
                    }
                }

                Map<String, String> exp = new HashMap<>();
                if (itemObj.has("exp") && itemObj.get("exp").isJsonObject()) {
                    JsonObject expObj = itemObj.getAsJsonObject("exp");
                    for (Map.Entry<String, JsonElement> entry : expObj.entrySet()) {
                        exp.put(entry.getKey(), entry.getValue().getAsString());
                    }
                }

                ConfigItem item = new ConfigItem(id, type, resourceType, rarity, right, exp);
                ALL_ITEMS.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}