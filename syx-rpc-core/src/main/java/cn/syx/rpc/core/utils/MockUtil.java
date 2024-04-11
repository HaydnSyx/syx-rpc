package cn.syx.rpc.core.utils;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockUtil {

    public static Object mock(Class<?> cls) {
        // 基础类型mock
        if (cls == int.class || cls == Integer.class) {
            return 1;
        } else if (cls == long.class || cls == Long.class) {
            return 2L;
        } else if (cls == double.class || cls == Double.class) {
            return 3.0;
        } else if (cls == float.class || cls == Float.class) {
            return 4.0f;
        } else if (cls == boolean.class || cls == Boolean.class) {
            return true;
        } else if (cls == char.class || cls == Character.class) {
            return '5';
        } else if (cls == byte.class || cls == Byte.class) {
            return (byte) 6;
        } else if (cls == short.class || cls == Short.class) {
            return (short) 7;
        }

        // 字符串类型
        if (cls == String.class) {
            return "mock-syx";
        }

        // 集合类型
        if (cls == List.class) {
            return Collections.emptyList();
        } else if (cls == Set.class) {
            return Collections.emptySet();
        } else if (cls == Map.class) {
            return Collections.emptyMap();
        }

        // pojo类型
        return mockPojo(cls);
    }

    @Nullable
    private static Object mockPojo(Class<?> cls) {
        try {
            Object obj = cls.getDeclaredConstructor().newInstance();
            Field[] fields = cls.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                field.set(obj, mock(field.getType()));
            }
            return obj;
        } catch (Exception e) {
            return null;
        }
    }
}
