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
        String encodedStr = Base64.getUrlEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
        return encodedStr.replaceAll("=+$", "");
    }

    public static String decode(String str) {
        if (!str.endsWith("==")) {
            str += "==";
        }
        return new String(Base64.getUrlDecoder().decode(str));
    }

    public static String tail(String str, int len) {
        if (str.length() <= len) {
            return str;
        }
        return str.substring(str.length() - len);
    }

    public static String removeSuffix(String str, String suffix) {
        if (str.endsWith(suffix)) {
            return str.substring(0, str.length() - suffix.length());
        }
        return str;
    }

    public static String convertToCamelCase(String input) {
        StringBuilder result = new StringBuilder();
        String[] words = input.toLowerCase().split("_");

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (i > 0 && word.length() > 0) {
                word = Character.toUpperCase(word.charAt(0)) + word.substring(1);
            }
            result.append(word);
        }

        return result.toString();
    }

    public static String convertToSnakeCase(String input) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);

            if (Character.isUpperCase(currentChar)) {
                result.append("_").append(Character.toLowerCase(currentChar));
            } else {
                result.append(currentChar);
            }
        }

        return result.toString().toUpperCase();
    }

    public static Class<?> getPrimitiveType(Class<?> wrapperClass) {
        if (wrapperClass == Integer.class) {
            return int.class;
        } else if (wrapperClass == Double.class) {
            return double.class;
        } else if (wrapperClass == Boolean.class) {
            return boolean.class;
        } else if (wrapperClass == Character.class) {
            return char.class;
        } else if (wrapperClass == Byte.class) {
            return byte.class;
        } else if (wrapperClass == Short.class) {
            return short.class;
        } else if (wrapperClass == Long.class) {
            return long.class;
        } else if (wrapperClass == Float.class) {
            return float.class;
        } else if (wrapperClass == String.class) {
            return String.class;
        } else {
            return null;
        }
    }
}
