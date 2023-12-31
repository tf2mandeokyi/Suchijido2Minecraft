package com.mndk.scjdmc.util;

import java.util.Map;

public class IntegerMapUtils {

    public static <T> int increment(Map<T, Integer> map, T key, int start) {
        int count;
        if(!map.containsKey(key)) {
            map.put(key, count = start);
        }
        else {
            count = map.get(key);
            map.put(key, ++count);
        }
        return count;
    }

}
