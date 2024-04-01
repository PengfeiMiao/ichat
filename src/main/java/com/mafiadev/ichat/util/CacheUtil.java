package com.mafiadev.ichat.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.mafiadev.ichat.constant.Constant.SEARCH_FAILED;

public class CacheUtil {
    private static final Map<String, String> cache = new ConcurrentHashMap<>();
    private static final long expires = 10 * 60 * 1000;

    public static <T> T cacheFunc(String key, Function<Void, T> function) {
        long current = System.currentTimeMillis();
        if (cache.containsKey(key)) {
            String obj = cache.get(key);
            int timeIdx = obj.indexOf("&");
            long last = Long.parseLong(obj.substring(0, timeIdx));
            if (current - last < expires) {
                return JSONObject.parseObject(obj.substring(timeIdx, obj.length() - 1), new TypeReference<T>() {
                });
            }
        }
        T result = function.apply(null);
        if (result != null && !SEARCH_FAILED.equals(String.valueOf(result))) {
            cache.put(key, current + "&" + JSON.toJSONString(result));
        }
        return result;
    }

    public static void reset() {
        cache.clear();
    }
}
