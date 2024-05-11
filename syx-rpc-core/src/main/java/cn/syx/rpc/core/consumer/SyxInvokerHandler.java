package cn.syx.rpc.core.consumer;

import cn.syx.rpc.core.api.*;
import cn.syx.rpc.core.filter.FilterChain;
import cn.syx.rpc.core.meta.InstanceMeta;
import cn.syx.rpc.core.utils.MethodUtil;
import cn.syx.rpc.core.utils.TypeUtil;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SyxInvokerHandler implements InvocationHandler {

    private final Class<?> cls;
    protected final RpcContext rpcContext;
    protected final RpcConsumerContext rpcConsumerContext;
    protected final List<InstanceMeta> providerList;
    /**
     * 方法对应的过滤器链表
     */
    private final Map<String, FilterChain> filterChainMap = new HashMap<>();

    public SyxInvokerHandler(Class<?> cls, RpcContext context, RpcConsumerContext consumerContext, List<InstanceMeta> providerList) {
        this.cls = cls;
        this.rpcContext = context;
        this.rpcConsumerContext = consumerContext;
        this.providerList = providerList;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 1.前置处理
        Object result = preCheck(proxy, method, args);
        if (Objects.nonNull(result)) {
            return result;
        }

        // 2.生成请求内容
        RpcRequest request = generateRpcRequest(method, args);

        // 3.生成过滤器链表
        Map<String, WrapperFilter> filterMap = rpcContext.getFilterMap();
        FilterChain filterChain = getFilterChain(request.getMethodSign(), method, filterMap);

        // 4.获取重试次数（只有超时异常才去重试）
        int retryNum = getMethodRetryNum(method);
        log.debug("===> parse retryNum: {}", retryNum);

        while (retryNum-- > 0) {
            // 5.执行调用
            try {
                result = doInvoke(method, filterChain, request);
                if (Objects.nonNull(result)) {
                    break;
                }
            } catch (Exception ex) {
                log.error("===> rpc invoke error: {}", ex.getMessage());
                if (!(ex.getCause() instanceof SocketTimeoutException)) {
                    throw ex;
                }
            }
        }

        return result;
    }

    protected Object preCheck(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        String methodName = method.getName();
        if (MethodUtil.isLocalMethod(methodName)) {
            return method.invoke(proxy, args);
        }
        return null;
    }

    protected RpcRequest generateRpcRequest(Method method, Object[] args) {
        RpcRequest request = new RpcRequest();
        request.setService(cls.getCanonicalName());
        request.setArgs(args);
        // 生成方法签名
        request.setMethodSign(MethodUtil.generateMethodSign(method));
        return request;
    }

    protected FilterChain getFilterChain(String methodSign, Method method, Map<String, WrapperFilter> filterMap) {
        FilterChain filterChain = filterChainMap.get(methodSign);
        if (Objects.nonNull(filterChain)) {
            log.debug("===> {} cache filter chain: {}", methodSign,
                    JSON.toJSONString(filterChain.getFilters().stream().map(e -> e.getClass().getSimpleName()).collect(Collectors.toList())));
            return filterChain;
        }

        List<String> filterNames = new ArrayList<>();

        String methodName = method.getName();
        List<String> methodFilterList = rpcConsumerContext.getFilterMap().get(methodName);
        List<String> filterNameList = rpcConsumerContext.getFilters();

        if (Objects.nonNull(methodFilterList)) {
            filterNames.addAll(methodFilterList);
        } else {
            filterNames.addAll(filterNameList);
        }

        filterChain = FilterChain.create(filterMap, filterNames);

        log.debug("===> {} create filter chain: {}", methodSign,
                JSON.toJSONString(filterChain.getFilters().stream().map(e -> e.getClass().getSimpleName()).collect(Collectors.toList())));
        filterChainMap.put(methodSign, filterChain);
        return filterChain;
    }

    protected int getMethodRetryNum(Method method) {
        String methodName = method.getName();
        int retryNum = rpcConsumerContext.getRetries();
        Map<String, Integer> methodRetryMap = rpcConsumerContext.getRetrytMap();
        if (Objects.nonNull(methodRetryMap)) {
            Integer num = methodRetryMap.get(methodName);
            if (Objects.nonNull(num) && num > 0) {
                retryNum = num;
            }
        }
        return retryNum;
    }

    @Nullable
    private Object doInvoke(Method method, FilterChain filterChain, RpcRequest request) throws Exception {
        Object result;

        // 1.前置过滤处理
        Object filterResult = filterChain.preFilter(request);
        if (filterResult != null) {
            log.debug("前置过滤处理结果: {}", filterResult);
            return filterResult;
        }

        // 2.获取服务实例
        InstanceMeta instance = calcInstanceMeta();

        RpcResponse<?> response = null;
        String url = instance.toUrl();
        try {
            preInvoke(instance, request, url);
            // 3.序列化请求
            byte[] bytes = rpcContext.getSerializer().serialize(request);

            // 4.执行远程调用
            response = rpcContext.getTransporter().invoke(bytes, url);
            postInvoke(instance, response);
        } catch (Exception exception) {
            exceptionInvoke(instance, request, url, exception);
        }

        // 5.解析结果
        result = rpcContext.getSerializer().deserialize(response, method.getGenericReturnType());

        // 6.后置过滤处理
        result = filterChain.postFilter(request, response, result);
        log.debug("后置过滤处理结果: {}", response);

        return result;
    }

    protected InstanceMeta calcInstanceMeta() {
        // 获取服务提供者
        List<InstanceMeta> instances = rpcContext.getRouter().route(providerList);
        return rpcContext.getLoadBalancer().choose(instances);
    }

    public void preInvoke(InstanceMeta instance, RpcRequest request, String url) {
    }

    public void postInvoke(InstanceMeta instance, RpcResponse<?> response) {
    }

    public void exceptionInvoke(InstanceMeta instance, RpcRequest request, String url, Exception exception) throws Exception {
        throw exception;
    }
}