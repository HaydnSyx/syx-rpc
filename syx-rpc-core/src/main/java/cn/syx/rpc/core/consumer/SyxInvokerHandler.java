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

    private Class<?> cls;

    private static final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient client = new OkHttpClient().newBuilder()
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
        request.setParameterTypes(method.getParameterTypes());

        Class<?> returnCls = method.getReturnType();

        RpcResponse response = post(request);
        if (response.isStatus()) {
            Object data = response.getData();
            if (data instanceof JSONObject) {
                return ((JSONObject) data).toJavaObject(returnCls);
            } else {
                return data;
            }
        }

        Exception ex = response.getEx();
        throw new RuntimeException(ex);
    }

    private RpcResponse post(RpcRequest request) throws IOException {
        String body = JSON.toJSONString(request);

        Request req = new Request.Builder()
                .url("http://localhost:8080/")
                .post(RequestBody.create(body, mediaType))
                .build();
        String data = client.newCall(req).execute().body().string();
        System.out.println("provider response ======> " + data);
        RpcResponse response = JSON.parseObject(data, RpcResponse.class);
        return response;
    }
}
