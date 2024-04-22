package com.mafiadev.ichat.util;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigUtil {
    private static final Map<String, List<String>> configMap = new HashMap<>();

    static {
        String yamlFilePath = "config.yml";
        Yaml yaml = new Yaml();

        try (InputStream in = ConfigUtil.class.getClassLoader().getResourceAsStream(yamlFilePath)) {
            Map<String, Object> config = yaml.load(in);
            for(String key : config.keySet()) {
                Object value = config.get(key);
                List<String> result = new ArrayList<>();
                if (value instanceof ArrayList<?>) {
                    for (Object o : (List<?>) value) {
                        result.add(String.valueOf(o));
                    }
                } else {
                    result.add(String.valueOf(value));
                }
                configMap.put(key, result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getConfigArr(String key) {
        return configMap.get(key);
    }

    public static String getConfig(String key) {
        List<String> list = getConfigArr(key);
        if(list == null || list.size() == 0) {
            return "";
        }
        return list.get(0);
    }
}
