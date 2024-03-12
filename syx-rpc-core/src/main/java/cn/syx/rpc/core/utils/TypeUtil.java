package cn.syx.rpc.core.utils;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

public class TypeUtil {

    // 对目标对象按照指定Class类型转换
    public static Object cast(Object obj, Class<?> clazz) {
        if (obj == null || clazz == null) {
            return null;
        }
        Class<?> objClass = obj.getClass();
        if (objClass.isAssignableFrom(clazz)) {
            return null;
        }

        if (obj instanceof Map map) {
            JSONObject jsonObject = new JSONObject(map);
            return jsonObject.toJavaObject(clazz);
        }

        // 基础类型转换
        if (clazz.isPrimitive()) {
            if (clazz == int.class || clazz == Integer.class) {
                return Integer.valueOf(String.valueOf(obj));
            } else if (clazz == long.class || clazz == Long.class) {
                return Long.valueOf(String.valueOf(obj));
            } else if (clazz == double.class || clazz == Double.class) {
                return Double.valueOf(String.valueOf(obj));
            } else if (clazz == float.class || clazz == Float.class) {
                return Float.valueOf(String.valueOf(obj));
            } else if (clazz == boolean.class || clazz == Boolean.class) {
                return Boolean.valueOf(String.valueOf(obj));
            } else if (clazz == byte.class || clazz == Byte.class) {
                return Byte.valueOf(String.valueOf(obj));
            } else if (clazz == char.class || clazz == Character.class) {
                return String.valueOf(obj).charAt(0);
            } else if (clazz == short.class || clazz == Short.class) {
                return Short.valueOf(String.valueOf(obj));
            }
        }

        return null;
    }
}
