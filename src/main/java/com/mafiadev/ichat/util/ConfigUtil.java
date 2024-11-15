package com.mafiadev.ichat.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
    private static JSONObject configJson = new JSONObject();

    static {
        loadConfig();
    }

    private static void loadConfig() {
        String yamlFilePath = "config.yml";
        Yaml yaml = new Yaml();

        try (InputStream in = ConfigUtil.class.getClassLoader().getResourceAsStream(yamlFilePath)) {
            Map<String, Object> config = yaml.load(in);
            configJson = JSON.parseObject(JSON.toJSONString(config));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> List<T> getConfigArr(@NotNull String key, Class<T> clazz) {
        String[] path = key.split("\\.");
        int index = 0;
        JSONObject curJson = configJson;
        while (index < path.length - 1) {
            curJson = (JSONObject) curJson.get(path[index]);
            if (curJson == null) {
                return null;
            }
            index++;
        }
        String leaf = path[path.length - 1];
        if (curJson.get(leaf) instanceof JSONObject || curJson.get(leaf) instanceof JSONArray) {
            JSONArray jsonArray = curJson.getJSONArray(leaf);
            return jsonArray.toJavaList(clazz);
        }
        Object res = curJson.get(leaf);
        if (res == null) {
            return null;
        }
        try {
            return Collections.singletonList((T) res);
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
