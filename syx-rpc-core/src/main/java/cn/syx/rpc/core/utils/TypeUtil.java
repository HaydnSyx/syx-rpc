package cn.syx.rpc.core.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.Map;


public class TypeUtil {

    // 对目标对象按照指定Class类型转换
    public static Object cast(Object obj, Class<?> clazz) {
        if (obj == null || clazz == null) {
            return null;
        }
        Class<?> objClass = obj.getClass();
        if (clazz.isAssignableFrom(objClass)) {
            return obj;
        }

        return JSON.to(clazz, JSON.toJSONString(obj));

        /*if (clazz.isArray()) {
            return JSON.to(clazz, JSON.toJSONString(obj));
        }

        if (obj instanceof JSONObject data) {
            return JSON.to(clazz, data);
        }

        if (obj instanceof JSONArray data) {
            return JSON.to(clazz, data);
        }

        if (obj instanceof Map map) {
            JSONObject jsonObject = new JSONObject(map);
            return jsonObject.toJavaObject(clazz);
        }

        // 基础类型转换
        if (obj instanceof Number data) {
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
        } else if (obj instanceof Boolean data && (clazz == boolean.class || clazz == Boolean.class)) {
            return data;
        } else if (obj instanceof Character data && (clazz == char.class || clazz == Character.class)) {
            return data;
        } else if (obj instanceof String data && clazz == String.class) {
            return data;
        }

        return null;*/
    }
}
