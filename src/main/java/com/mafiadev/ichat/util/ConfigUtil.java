package com.mafiadev.ichat.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConfigUtil {
    private static final Gson gson = new Gson();
    private static JsonObject configJson = new JsonObject();

    static {
        loadConfig();
    }

    private static void loadConfig() {
        String yamlFilePath = "config.yml";
        Yaml yaml = new Yaml();

        try (InputStream in = ConfigUtil.class.getClassLoader().getResourceAsStream(yamlFilePath)) {
            Map<String, Object> config = yaml.load(in);
            String json = gson.toJson(config);
            configJson = gson.fromJson(json, JsonObject.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> List<T> getConfigArr(@NotNull String key, Class<T> clazz) {
        try {
            String[] path = key.split("\\.");
            int index = 0;
            JsonObject curJson = configJson;
            while (index < path.length - 1) {
                curJson = curJson.getAsJsonObject(path[index]);
                if (curJson == null) {
                    return null;
                }
                index++;
            }
            String leaf = path[path.length - 1];
            JsonElement leafElement = curJson.get(leaf);
            if (leafElement == null) {
                return null;
            }
            if (leafElement.isJsonArray()) {
                JsonArray jsonArray = leafElement.getAsJsonArray();
                return jsonArray
                        .asList().stream()
                        .map(element -> gson.fromJson(element, clazz))
                        .collect(Collectors.toList());
            } else {
                return Collections.singletonList(gson.fromJson(leafElement, clazz));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> getConfigArr(@NotNull String key) {
        return Optional.ofNullable(getConfigArr(key, Object.class)).orElse(new ArrayList<>()).stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    public static String getConfig(@NotNull String key) {
        List<String> list = getConfigArr(key);
        if (list == null || list.size() == 0) {
            return "";
        }
        return list.get(0);
    }
}
