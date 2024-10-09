package com.mafiadev.ichat.util;

import com.mafiadev.ichat.constant.Constant;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class ConfigUtil {
    private static final Map<String, List<String>> configMap = new HashMap<>();

    static {
//        updateDbUrl();
        loadConfig();
    }

    private static void updateDbUrl() {
        Properties prop = new Properties();
        ClassLoader classLoader = ConfigUtil.class.getClassLoader();
        String config = "hibernate.cfg.xml";
        Path resourcePath = Paths.get(Objects.requireNonNull(classLoader.getResource(config)).getPath());
        try(InputStream input = Files.newInputStream(resourcePath) ;
            OutputStream output = Files.newOutputStream(resourcePath)) {
            prop.load(input);
            prop.setProperty("hibernate.connection.url", "jdbc:sqlite:" + Constant.DB_PATH);
            prop.store(output, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadConfig() {
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
