package com.mafiadev.ichat.util;

import org.apache.commons.text.similarity.JaroWinklerDistance;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

public class CommonUtil {
    public static int randomIndex(int bound) {
        Random random = new Random();
        return random.nextInt(bound);
    }

    public static boolean isSimilar(String text1, String text2, double threshold) {
        return new JaroWinklerDistance().apply(text1, text2) < threshold;
    }

    public static String encode(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    public static String decode(String str) {
        return new String(Base64.getDecoder().decode(str));
    }

    public static String tail(String str, int len) {
        if (str.length() <= len) {
            return str;
        }
        return str.substring(str.length() - len);
    }
}
