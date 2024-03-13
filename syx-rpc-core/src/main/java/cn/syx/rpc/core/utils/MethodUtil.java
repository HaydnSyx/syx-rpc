package cn.syx.rpc.core.utils;

import java.lang.reflect.Method;

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
}
