package cn.syx.rpc.core.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MethodUtil {

    public static boolean isLocalMethod(final String methodName) {
        return "toSting".equals(methodName)
                || "hashCode".equals(methodName)
                || "equals".equals(methodName)
                || "getClass".equals(methodName)
                || "notify".equals(methodName)
                || "notifyAll".equals(methodName)
                || "wait".equals(methodName)
                || "finalize".equals(methodName)
                || "clone".equals(methodName);
    }

    public static boolean isLocalMethod(Method method) {
        return isLocalMethod(method.getName());
    }

    public static String generateMethodSign(Method method) {
        StringBuilder sb = new StringBuilder(method.getName());
        sb.append("(");
        for (Class<?> parameterType : method.getParameterTypes()) {
            sb.append(parameterType.getCanonicalName()).append(",");
        }
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(")");
        return sb.toString();
    }

    public static Method findMethodByMethodSign(Method[] methods, String methodSign) {
        for (Method method : methods) {
            if (MethodUtil.isLocalMethod(method)) {
                continue;
            }

            String sign = MethodUtil.generateMethodSign(method);
            if (Objects.equals(sign, methodSign)) {
                return method;
            }
        }
        return null;
    }

    public static List<Field> findAnnotationField(Class<?> cls, Class<? extends Annotation> annotationCls) {
        List<Field> result = new ArrayList<>();
        while (cls != null) {
            Field[] fields = cls.getDeclaredFields();
            if (fields.length == 0) {
                cls = cls.getSuperclass();
                continue;
            }

            result.addAll(Arrays.stream(fields)
                    .filter(e -> e.isAnnotationPresent(annotationCls))
                    .toList());
            cls = cls.getSuperclass();
        }

        return result;
    }
}
