package cn.syx.rpc.core.consumer;

import cn.syx.rpc.core.api.*;
import cn.syx.rpc.core.consumer.http.OkHttpInvoker;
import cn.syx.rpc.core.governance.SlidingTimeWindow;
import cn.syx.rpc.core.meta.InstanceMeta;
import cn.syx.rpc.core.utils.MethodUtil;
import cn.syx.rpc.core.utils.TypeUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class SyxInvokerHandler implements InvocationHandler {

    private final Class<?> cls;
    private final RpcContext rpcContext;
    private final List<InstanceMeta> providerList;

    private final Set<InstanceMeta> isolateProviderList = new HashSet<>();

    private final List<InstanceMeta> halfOpenProviderList = new ArrayList<>();

    Map<String, SlidingTimeWindow> windows = new HashMap<>();

    private final HttpInvoker invoker = new OkHttpInvoker();

    private final ScheduledExecutorService executorService;

    public SyxInvokerHandler(Class<?> cls, RpcContext context, List<InstanceMeta> providerList) {
        this.cls = cls;
        this.rpcContext = context;
        this.providerList = providerList;
        this.executorService = Executors.newScheduledThreadPool(1);
        this.executorService.scheduleWithFixedDelay(this::halfOpen,
                10, 60, java.util.concurrent.TimeUnit.SECONDS);
    }

    private void halfOpen() {
        log.debug("===> half open isolateProviderList: {}", isolateProviderList);
        halfOpenProviderList.clear();
        halfOpenProviderList.addAll(isolateProviderList);
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

                InstanceMeta instance = null;
                synchronized (halfOpenProviderList) {
                    if (halfOpenProviderList.isEmpty()) {
                        // 获取服务提供者
                        List<InstanceMeta> instances = rpcContext.getRouter().route(providerList);
                        instance = rpcContext.getLoadBalancer().choose(instances);
                        log.debug("real provider ======> {}", instance);
                    } else {
                        instance = halfOpenProviderList.remove(0);
                        log.debug("half open provider ======> {}", instance);
                    }
                }

                RpcResponse<?> response = null;
                Object result = null;
                String url = instance.toUrl();

                try {
                    response = invoker.post(request, url);
                    result = castResponse(method, response);
                } catch (Exception e) {

                    SlidingTimeWindow window = windows.get(url);
                    if (Objects.isNull(window)) {
                        window = new SlidingTimeWindow();
                        windows.put(url, window);
                    }

                    window.record(System.currentTimeMillis());
                    log.debug("===> instance: {}, window: {}", url, window.getSum());

                    // 发生10次则进行故障隔离
                    if (window.getSum() >= 10) {
                        isolate(instance);
                    }

                    throw e;
                }

                synchronized (providerList){
                    if (!providerList.contains(instance)) {
                        isolateProviderList.remove(instance);
                        providerList.add(instance);
                        log.debug("===> instance recovery: {}, isolates={}, providers={}",
                                instance, isolateProviderList, providerList);
                    }
                }

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

    private void isolate(InstanceMeta instance) {
        log.info("===> instance: {} is isolated", instance);
        providerList.remove(instance);
        log.debug("===> providerList: {}", providerList);
        isolateProviderList.add(instance);
        log.debug("===> isolateProviderList: {}", isolateProviderList);
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