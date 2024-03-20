package cn.syx.rpc.core.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;


public class TypeUtil {

    // 对目标对象按照指定Class类型转换
    public static Object castV1(Object obj, Type type) {
        if (obj == null || type == null) {
            return null;
        }

        return JSON.parseObject(JSON.toJSONString(obj), type);
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

    private static Object castObject(Method method, Object data) {
        Class<?> type = method.getReturnType();
        if (data instanceof JSONObject jsonResult) {
            if (Map.class.isAssignableFrom(type)) {
                Map resultMap = new HashMap();
                Type genericReturnType = method.getGenericReturnType();
                System.out.println(genericReturnType);
                if (genericReturnType instanceof ParameterizedType parameterizedType) {
                    Class<?> keyType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                    Class<?> valueType = (Class<?>) parameterizedType.getActualTypeArguments()[1];
                    System.out.println("keyType  : " + keyType);
                    System.out.println("valueType: " + valueType);
                    jsonResult.entrySet().stream().forEach(
                            e -> {
                                Object key = TypeUtil.castV2(e.getKey(), keyType);
                                Object value = TypeUtil.castV2(e.getValue(), valueType);
                                resultMap.put(key, value);
                            }
                    );
                }
                return resultMap;
            }
            return jsonResult.toJavaObject(type);
        } else if (data instanceof JSONArray jsonArray) {
            Object[] array = jsonArray.toArray();
            if (type.isArray()) {
                Class<?> componentType = type.getComponentType();
                Object resultArray = Array.newInstance(componentType, array.length);
                for (int i = 0; i < array.length; i++) {
                    Array.set(resultArray, i, array[i]);
                }
                return resultArray;
            } else if (List.class.isAssignableFrom(type)) {
                List<Object> resultList = new ArrayList<>(array.length);
                Type genericReturnType = method.getGenericReturnType();
                System.out.println(genericReturnType);
                if (genericReturnType instanceof ParameterizedType parameterizedType) {
                    Type actualType = parameterizedType.getActualTypeArguments()[0];
                    System.out.println(actualType);
                    for (Object o : array) {
                        resultList.add(TypeUtil.castV2(o, (Class<?>) actualType));
                    }
                } else {
                    resultList.addAll(Arrays.asList(array));
                }
                return resultList;
            } else {
                return null;
            }
        } else {
            return TypeUtil.castV2(data, type);
        }
    }
}
