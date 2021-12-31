package com.mndk.scjdmc.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionUtil {
    public static Field[] getAllFields(Class<?> clazz, Class<?> exclude) {
        List<Field> result = new ArrayList<>();
        Class<?> temp = clazz;
        do {
            result.addAll(Arrays.asList(temp.getDeclaredFields()));
            temp = temp.getSuperclass();
        } while(temp != null && !temp.equals(exclude));
        return result.toArray(new Field[0]);
    }
}
