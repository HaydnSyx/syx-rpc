package cn.syx.rpc.core.consumer;

import cn.syx.rpc.core.api.*;
import cn.syx.rpc.core.utils.MethodUtil;
import cn.syx.rpc.core.utils.TypeUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class SyxInvokerHandler implements InvocationHandler {

    private final Class<?> cls;
    private final RpcContext rpcContext;
    private final List providerList;

    private static final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

    private static final OkHttpClient client = new OkHttpClient().newBuilder()
            .connectionPool(new ConnectionPool(16, 1, TimeUnit.MINUTES))
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .connectTimeout(1, TimeUnit.SECONDS)
            .build();

    public SyxInvokerHandler(Class<?> cls, RpcContext context, List<String> providerList) {
        this.cls = cls;
        this.rpcContext = context;
        this.providerList = providerList;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // 判断基础类型
        String methodName = method.getName();
        if (MethodUtil.isLocalMethod(methodName)) {
            return method.invoke(proxy, args);
        }

        RpcRequest request = new RpcRequest();
        request.setService(cls.getCanonicalName());
        request.setArgs(args);
        // 生成方法签名
        request.setMethodSign(MethodUtil.generateMethodSign(method));

        // 获取服务提供者
        List<String> routeList = rpcContext.getRouter().route(providerList);
        String provider = (String) rpcContext.getLoadBalancer().choose(routeList);
        System.out.println("real provider ======> " + provider);

        RpcResponse<?> response = post(request, provider);
        if (response.isStatus()) {
            Object data = response.getData();
            Type type = method.getGenericReturnType();
            return TypeUtil.castV1(data, type);
//            return castObject(method, data);
        }

        Exception ex = response.getEx();
        throw new RuntimeException(ex);
    }

    @Nullable
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

    private RpcResponse<?> post(RpcRequest request, String url) throws IOException {
        String body = JSON.toJSONString(request);

        Request req = new Request.Builder()
                .url(url)
                .post(RequestBody.create(body, mediaType))
                .build();
        String data = client.newCall(req).execute().body().string();
        System.out.println("provider response ======> " + data);
        return JSON.parseObject(data, RpcResponse.class);
    }
}
