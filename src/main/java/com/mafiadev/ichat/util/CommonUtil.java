package com.mafiadev.ichat.util;

import org.apache.commons.text.similarity.JaroWinklerDistance;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtil {
    public static int randomIndex(int bound) {
        Random random = new Random();
        return random.nextInt(bound);
    }

    public static boolean isSimilar(String text1, String text2, double threshold) {
        return new JaroWinklerDistance().apply(text1, text2) < threshold;
    }

    public static String encode(String str) {
        return Base64.getUrlEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    public static String decode(String str) {
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

    public static String digest(String input) {
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes());
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sb.toString();
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

    public static boolean isMatch(String str, String regex) {
        Pattern r2 = Pattern.compile(regex);
        Matcher m2 = r2.matcher(str);
        return m2.find();
    }
}
