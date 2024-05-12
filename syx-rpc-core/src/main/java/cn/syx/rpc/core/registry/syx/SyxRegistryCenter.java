package cn.syx.rpc.core.registry.syx;

import cn.syx.registry.client.SyxRegistryClient;
import cn.syx.registry.client.model.SyxRegistryInstanceMeta;
import cn.syx.rpc.core.api.RegistryCenter;
import cn.syx.rpc.core.consumer.HttpInvoker;
import cn.syx.rpc.core.meta.InstanceMeta;
import cn.syx.rpc.core.meta.ServiceMeta;
import cn.syx.rpc.core.registry.RegistryChangeListener;
import cn.syx.rpc.core.registry.RegistryChangeEvent;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.stream.Collectors;

@Slf4j
public class SyxRegistryCenter implements RegistryCenter {

//    @Value("${syxregistry.services}")
//    private String services;

    private Map<String, Long> VERSIONS = new HashMap<>();
//    MultiValueMap<InstanceMeta, ServiceMeta> RENEWS = new LinkedMultiValueMap<>();
    private ScheduledExecutorService executorService;
//    private ScheduledExecutorService providerService;

    private SyxRegistryClient registryClient;

    public SyxRegistryCenter(SyxRegistryClient registryClient) {
        this.registryClient = registryClient;
    }

    @Override
    public void start() {
        log.info("===> SyxRegistryCenter started.");

        executorService = Executors.newScheduledThreadPool(1);
        /*providerService = Executors.newScheduledThreadPool(1);

        providerService.scheduleWithFixedDelay(() -> {
            RENEWS.keySet().forEach(e -> {
                String services = String.join(",", RENEWS.get(e).stream().map(ServiceMeta::toPath).toArray(String[]::new));
                log.info("===> SyxRegistryCenter renew instance: {} for services: {}", e, services);
                HttpInvoker.httpPost(JSON.toJSONString(e), this.services + "/renews?services=" + services, Long.class);
            });
        }, 5000, 5000, TimeUnit.MILLISECONDS);*/
    }

    @Override
    public void stop() {
        log.info("===> SyxRegistryCenter stopped.");
        gracefulShutdown(executorService);
//        gracefulShutdown(providerService);
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
        registryClient.register(service.toPath(), convert(instance));
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        registryClient.unregister(service.toPath(), convert(instance));
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        List<SyxRegistryInstanceMeta> instanceMetas = registryClient.fetchAll(service.toPath());
        return instanceMetas.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public void subscribe(ServiceMeta service, RegistryChangeListener listener) {
        executorService.scheduleWithFixedDelay(() -> {
            // 获取当前服务版本
            Long version = VERSIONS.get(service.toPath());
            if (Objects.isNull(version)) {
                version = -1L;
            }

            Long newVersion = registryClient.version(service.toPath());
            log.info("===> SyxRegistryCenter subscribe service: {}, version: {}, newVersion: {}", service, version, newVersion);
            if (newVersion > version) {
                // 获取最新的服务实例
                List<InstanceMeta> nodes = fetchAll(service);
                listener.fire(new RegistryChangeEvent(nodes));
                VERSIONS.put(service.toPath(), newVersion);
            }
        }, 1000, 5000, TimeUnit.MILLISECONDS);
    }

    private SyxRegistryInstanceMeta convert(InstanceMeta instanceMeta) {
        SyxRegistryInstanceMeta meta = new SyxRegistryInstanceMeta();
        meta.setHost(instanceMeta.getHost());
        meta.setPort(instanceMeta.getPort());
        meta.setSchema(instanceMeta.getSchema());
        meta.setPath(instanceMeta.getPath());
        meta.setStatus(instanceMeta.isStatus());
        meta.setParameters(instanceMeta.getParameters());
        return meta;
    }

    private InstanceMeta convert(SyxRegistryInstanceMeta instanceMeta) {
        InstanceMeta meta = new InstanceMeta();
        meta.setHost(instanceMeta.getHost());
        meta.setPort(instanceMeta.getPort());
        meta.setSchema(instanceMeta.getSchema());
        meta.setPath(instanceMeta.getPath());
        meta.setStatus(instanceMeta.isStatus());
        meta.setParameters(instanceMeta.getParameters());
        return meta;
    }
}
