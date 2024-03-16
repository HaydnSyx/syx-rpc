package cn.syx.rpc.core.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;


public class TypeUtil {

    // 对目标对象按照指定Class类型转换
    public static Object castV1(Object obj, Class<?> clazz) {
        if (obj == null || clazz == null) {
            return null;
        }
        Class<?> objClass = obj.getClass();
        if (clazz.isAssignableFrom(objClass)) {
            return obj;
        }

        return JSON.to(clazz, JSON.toJSONString(obj));
    }

    public static Object castV2(Object origin, Class<?> clazz) {
        if (origin == null || clazz == null) return null;

        Class<?> aClass = origin.getClass();
        if (clazz.isAssignableFrom(aClass)) {
            return origin;
        }

        if (clazz.isArray()) {
            if (origin instanceof List list) {
                origin = list.toArray();
            }
            int length = Array.getLength(origin);
            Class<?> componentType = clazz.getComponentType();
            Object resultArray = Array.newInstance(componentType, length);
            for (int i = 0; i < length; i++) {
                Array.set(resultArray, i, Array.get(origin, i));
            }
            return resultArray;
        }

        if (origin instanceof HashMap map) {
            JSONObject jsonObject = new JSONObject(map);
            return jsonObject.toJavaObject(clazz);
        }

        if (origin instanceof JSONObject jsonObject) {
            return jsonObject.toJavaObject(clazz);
        }

        // 基础类型转换
        if (origin instanceof Number data) {
            if (clazz == int.class || clazz == Integer.class) {
                return data.intValue();
            } else if (clazz == long.class || clazz == Long.class) {
                return data.longValue();
            } else if (clazz == byte.class || clazz == Byte.class) {
                return data.byteValue();
            } else if (clazz == short.class || clazz == Short.class) {
                return data.shortValue();
            } else if (clazz == float.class || clazz == Float.class) {
                return data.floatValue();
            } else if (clazz == double.class || clazz == Double.class) {
                return data.doubleValue();
            }
        } else if (origin instanceof Boolean data && (clazz == boolean.class || clazz == Boolean.class)) {
            return data;
        } else if (origin instanceof Character data && (clazz == char.class || clazz == Character.class)) {
            return data;
        } else if (origin instanceof String data && clazz == String.class) {
            return data;
        }

        return null;
    }
}
