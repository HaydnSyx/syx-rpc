package cn.syx.rpc.core.consumer;

import cn.syx.rpc.core.api.RpcRequest;
import cn.syx.rpc.core.api.RpcResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SyxInvokerHandler implements InvocationHandler {

    private final Class<?> cls;

    private static final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

    private static final OkHttpClient client = new OkHttpClient().newBuilder()
            .connectionPool(new ConnectionPool(16, 1, TimeUnit.MINUTES))
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .connectTimeout(1, TimeUnit.SECONDS)
            .build();

    public SyxInvokerHandler(Class<?> cls) {
        this.cls = cls;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // 判断基础类型
        String methodName = method.getName();
        if (Objects.equals("toString", methodName)
                || Objects.equals("hashCode", methodName)
                || Objects.equals("equals", methodName)) {
            return method.invoke(proxy, args);
        }

        RpcRequest request = new RpcRequest();
        request.setService(cls.getCanonicalName());
        request.setMethod(methodName);
        request.setArgs(args);
//        request.setParameterTypes(method.getParameterTypes());
        // 生成方法签名
        StringBuilder sb = new StringBuilder();
        sb.append(cls.getCanonicalName()).append("#").append(methodName).append("(");
        for (Class<?> parameterType : method.getParameterTypes()) {
            sb.append(parameterType.getCanonicalName()).append(",");
        }
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(")");
        request.setMethodSign(sb.toString());

        Class<?> returnCls = method.getReturnType();

        RpcResponse<?> response = post(request);
        if (response.isStatus()) {
            return getObject(response.getData(), returnCls);
        }

        Exception ex = response.getEx();
        throw new RuntimeException(ex);
    }

    private RpcResponse<?> post(RpcRequest request) throws IOException {
        String body = JSON.toJSONString(request);

        Request req = new Request.Builder()
                .url("http://localhost:8080/")
                .post(RequestBody.create(body, mediaType))
                .build();
        String data = client.newCall(req).execute().body().string();
        System.out.println("provider response ======> " + data);
        RpcResponse<?> response = JSON.parseObject(data, RpcResponse.class);
        return response;
    }

    private static Object getObject(Object obj, Class<?> returnCls) {
        if (Objects.isNull(obj) || (returnCls == void.class || returnCls == Void.class)) {
            return null;
        }

        if (returnCls == Objects.class) {
            return obj;
        }

        if (obj instanceof JSONObject) {
            return ((JSONObject) obj).toJavaObject(returnCls);
        }

        if (returnCls.isInstance(obj)) {
            return obj;
        } else {
            if (obj instanceof Number data) {
                if (returnCls == int.class || returnCls == Integer.class) {
                    return data.intValue();
                } else if (returnCls == long.class || returnCls == Long.class) {
                    return data.longValue();
                } else if (returnCls == byte.class || returnCls == Byte.class) {
                    return data.byteValue();
                } else if (returnCls == short.class || returnCls == Short.class) {
                    return data.shortValue();
                } else if (returnCls == float.class || returnCls == Float.class) {
                    return data.floatValue();
                } else if (returnCls == double.class || returnCls == Double.class) {
                    return data.doubleValue();
                }
            } else if (obj instanceof Boolean data && (returnCls == boolean.class || returnCls == Boolean.class)) {
                return data;
            } else if (obj instanceof Character data && (returnCls == char.class || returnCls == Character.class)) {
                return data;
            } else if (obj instanceof String data && returnCls == String.class) {
                return data;
            }
            throw new RuntimeException("未能成功转换对应类型");
        }
    }
}
