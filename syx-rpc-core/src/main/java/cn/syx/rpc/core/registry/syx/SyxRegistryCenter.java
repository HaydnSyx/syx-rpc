package cn.syx.rpc.core.registry.syx;

import cn.syx.rpc.core.api.RegistryCenter;
import cn.syx.rpc.core.consumer.HttpInvoker;
import cn.syx.rpc.core.consumer.http.DynamicConnectTimeout;
import cn.syx.rpc.core.meta.InstanceMeta;
import cn.syx.rpc.core.meta.ServiceMeta;
import cn.syx.rpc.core.registry.ChangeListener;
import cn.syx.rpc.core.registry.Event;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SyxRegistryCenter implements RegistryCenter {

    @Value("${syxregistry.services}")
    private String services;
//    private OkHttpClient client;

    private Map<String, Long> VERSIONS = new HashMap<>();
    MultiValueMap<InstanceMeta, ServiceMeta> RENEWS = new LinkedMultiValueMap<>();
    private ScheduledExecutorService executorService;

//    private ScheduledExecutorService consumerService;
    private ScheduledExecutorService providerService;

    @Override
    public void start() {
        /*this.client = new OkHttpClient().newBuilder()
//                .addInterceptor(new DynamicConnectTimeout(consumerContext))
                .connectionPool(new ConnectionPool(16, 1, TimeUnit.MINUTES))
                .readTimeout(1000, TimeUnit.MILLISECONDS)
                .writeTimeout(1000, TimeUnit.MILLISECONDS)
                .connectTimeout(1000, TimeUnit.MILLISECONDS)
                .build();*/
        log.info("===> SyxRegistryCenter started.");

        executorService = Executors.newScheduledThreadPool(1);
//        consumerService = Executors.newScheduledThreadPool(1);
        // todo 优化在consumer启动时不执行下列代码
        providerService = Executors.newScheduledThreadPool(1);

        providerService.scheduleWithFixedDelay(() -> {
            RENEWS.keySet().forEach(e -> {
                String services = String.join(",", RENEWS.get(e).stream().map(ServiceMeta::toPath).toArray(String[]::new));
                log.info("===> SyxRegistryCenter renew instance: {} for services: {}", e, services);
                HttpInvoker.httpPost(JSON.toJSONString(e), this.services + "/renews?services=" + services, Long.class);
            });
        }, 5000, 5000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        log.info("===> SyxRegistryCenter stopped.");
        gracefulShutdown(executorService);
//        gracefulShutdown(consumerService);
        gracefulShutdown(providerService);
    }

    private void gracefulShutdown(ScheduledExecutorService executor) {
        executor.shutdown();
        try {
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("===> SyxRegistryCenter stop error", e);
        }
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        log.info("===> SyxRegistryCenter registry start instance: {} for service: {}", instance, service);
        HttpInvoker.httpPost(JSON.toJSONString(instance), services + "/reg?service=" + service.toPath(), InstanceMeta.class);
        log.info("===> SyxRegistryCenter registry end instance: {}", instance);
        RENEWS.add(instance, service);
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        log.info("===> SyxRegistryCenter unregistry start instance: {} for service: {}", instance, service);
        HttpInvoker.httpPost(JSON.toJSONString(instance), services + "/unreg?service=" + service.toPath(), InstanceMeta.class);
        log.info("===> SyxRegistryCenter unregistry end instance: {}", instance);
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        log.info("===> SyxRegistryCenter fetchAll start service: {}", service);
        List<InstanceMeta> instanceMetas = HttpInvoker.httpGet(services + "/findAll?service=" + service.toPath(), new TypeReference<List<InstanceMeta>>() {
        });
        log.info("===> SyxRegistryCenter fetchAll end service: {}, instances: {}", service, instanceMetas);
        return instanceMetas;
    }

    @Override
    public void subscribe(ServiceMeta service, ChangeListener listener) {
        executorService.scheduleWithFixedDelay(() -> {
            // 获取当前服务版本
            Long version = VERSIONS.get(service.toPath());
            if (Objects.isNull(version)) {
                version = -1L;
            }

            Long newVersion = HttpInvoker.httpGet(services + "/version?service=" + service.toPath(), Long.class);
            log.info("===> SyxRegistryCenter subscribe service: {}, version: {}, newVersion: {}", service, version, newVersion);
            if (newVersion > version) {
                // 获取最新的服务实例
                List<InstanceMeta> nodes = fetchAll(service);
                listener.fire(new Event(nodes));
                VERSIONS.put(service.toPath(), newVersion);
            }
        }, 1000, 5000, TimeUnit.MILLISECONDS);
    }
}
