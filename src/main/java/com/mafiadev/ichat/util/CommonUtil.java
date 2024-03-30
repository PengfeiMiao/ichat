package com.mafiadev.ichat.util;

import org.apache.commons.text.similarity.JaroWinklerDistance;

import java.util.Random;

public class CommonUtil {
    public static int randomIndex(int bound) {
        Random random = new Random();
        return random.nextInt(bound);
    }

    public static boolean isSimilar(String text1, String text2, double threshold) {
        return new JaroWinklerDistance().apply(text1, text2) < threshold;
    }
}
