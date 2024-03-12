package com.qs.common.config;

import java.util.List;
import java.util.Random;

/**
 * @author qiaoshuai
 * @date 2024/3/1
 */
public class RandomListElement {
    public static <T> T getRandomElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("List must not be null or empty");
        }

        Random random = new Random();
        int randomIndex = random.nextInt(list.size());
        return list.get(randomIndex);
    }
}
