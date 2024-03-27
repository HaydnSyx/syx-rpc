package cn.syx.rpc.core.consumer;

import cn.syx.rpc.core.api.*;
import cn.syx.rpc.core.consumer.http.OkHttpInvoker;
import cn.syx.rpc.core.meta.InstanceMeta;
import cn.syx.rpc.core.utils.MethodUtil;
import cn.syx.rpc.core.utils.TypeUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.util.List;

@Slf4j
public class SyxInvokerHandler implements InvocationHandler {

    private final Class<?> cls;
    private final RpcContext rpcContext;
    private final List<InstanceMeta> providerList;

    private final HttpInvoker invoker = new OkHttpInvoker();

    public SyxInvokerHandler(Class<?> cls, RpcContext context, List<InstanceMeta> providerList) {
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

        List<Filter> filters = rpcContext.getFilters();
        int retryNum = 2;
        while (retryNum-- > 0) {
            log.info(" ===> retryNum: {}", retryNum);

            try {
                // 前置过滤处理
                for (Filter filter : filters) {
                    Object result = filter.preFilter(request);
                    if (result != null) {
                        log.debug("前置过滤处理结果: {}", result);
                        return result;
                    }
                }

                // 获取服务提供者
                List<InstanceMeta> instances = rpcContext.getRouter().route(providerList);
                InstanceMeta instance = rpcContext.getLoadBalancer().choose(instances);
                log.debug("real provider ======> {}", instance);

                RpcResponse<?> response = invoker.post(request, instance.toUrl());
                Object result = castResponse(method, response);

                // 后置过滤处理
                for (Filter filter : filters) {
                    result = filter.postFilter(request, response, result);
                    log.debug("后置过滤处理结果: {}", response);
                }

                return result;
            } catch (Exception ex) {
                if (!(ex.getCause() instanceof SocketTimeoutException)) {
                    throw ex;
                }
            }
        }

        return null;
    }

    @Nullable
    private static Object castResponse(Method method, RpcResponse<?> response) {
        if (response.isStatus()) {
            Object data = response.getData();
            Type type = method.getGenericReturnType();
            return TypeUtil.castV1(data, type);
        }

        throw new RpcException(response.getEx());
    }
}