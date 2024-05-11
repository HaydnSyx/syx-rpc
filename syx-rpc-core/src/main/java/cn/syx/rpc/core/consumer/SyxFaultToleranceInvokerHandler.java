package cn.syx.rpc.core.consumer;

import cn.syx.rpc.core.api.RpcConsumerContext;
import cn.syx.rpc.core.api.RpcContext;
import cn.syx.rpc.core.api.RpcRequest;
import cn.syx.rpc.core.api.RpcResponse;
import cn.syx.rpc.core.governance.SlidingTimeWindow;
import cn.syx.rpc.core.meta.InstanceMeta;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class SyxFaultToleranceInvokerHandler extends SyxInvokerHandler {

    private final Set<InstanceMeta> isolateProviderList = new HashSet<>();

    private final List<InstanceMeta> halfOpenProviderList = new ArrayList<>();

    private final Map<String, SlidingTimeWindow> windows = new HashMap<>();

    public SyxFaultToleranceInvokerHandler(Class<?> cls, RpcContext context, RpcConsumerContext consumerContext, List<InstanceMeta> providerList) {
        super(cls, context, consumerContext, providerList);
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleWithFixedDelay(this::halfOpen,
                consumerContext.getHalfOpenInitialDelay(),
                consumerContext.getHalfOpenDelay(),
                java.util.concurrent.TimeUnit.SECONDS);
    }

    private void halfOpen() {
        log.debug("===> half open isolateProviderList: {}", isolateProviderList);
        halfOpenProviderList.clear();
        halfOpenProviderList.addAll(isolateProviderList);
    }

    @Override
    public void postInvoke(InstanceMeta instance, RpcResponse<?> response) {
        if (!rpcConsumerContext.isEnableFaultTolerance()) {
            super.postInvoke(instance, response);
        }

        synchronized (providerList) {
            if (!providerList.contains(instance)) {
                isolateProviderList.remove(instance);
                providerList.add(instance);
                log.debug("===> instance recovery: {}, isolates={}, providers={}",
                        instance, isolateProviderList, providerList);
            }
        }
    }

    @Override
    public void exceptionInvoke(InstanceMeta instance, RpcRequest request, String url, Exception exception) throws Exception {
        if (!rpcConsumerContext.isEnableFaultTolerance()) {
            super.exceptionInvoke(instance, request, url, exception);
        }

        SlidingTimeWindow window = windows.get(url);
        if (Objects.isNull(window)) {
            window = new SlidingTimeWindow();
            windows.put(url, window);
        }

        window.record(System.currentTimeMillis());
        log.debug("===> instance: {}, window: {}", url, window.getSum());

        // 发生10次则进行故障隔离
        if (window.getSum() >= rpcConsumerContext.getFaultLimit()) {
            isolate(instance);
        }

        throw exception;
    }

    @Override
    public InstanceMeta calcInstanceMeta() {
        if (!rpcConsumerContext.isEnableFaultTolerance()) {
            return super.calcInstanceMeta();
        }

        InstanceMeta instance;
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
        return instance;
    }

    private void isolate(InstanceMeta instance) {
        log.info("===> instance: {} is isolated", instance);
        providerList.remove(instance);
        log.debug("===> providerList: {}", providerList);
        isolateProviderList.add(instance);
        log.debug("===> isolateProviderList: {}", isolateProviderList);
    }
}