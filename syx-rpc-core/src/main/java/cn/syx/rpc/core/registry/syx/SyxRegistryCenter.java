package cn.syx.rpc.core.registry.syx;

import cn.syx.registry.client.SyxRegistryClient;
import cn.syx.registry.core.model.RegistryInstanceMeta;
import cn.syx.registry.core.model.instance.RpcServiceMeta;
import cn.syx.rpc.core.api.RegistryCenter;
import cn.syx.rpc.core.meta.InstanceMeta;
import cn.syx.rpc.core.registry.RegistryChangeEvent;
import cn.syx.rpc.core.registry.RegistryChangeListener;
import lombok.extern.slf4j.Slf4j;

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

    private Map<String, Long> VERSIONS = new HashMap<>();
    private ScheduledExecutorService executorService;

    private SyxRegistryClient registryClient;

    public SyxRegistryCenter(SyxRegistryClient registryClient) {
        this.registryClient = registryClient;
    }

    @Override
    public void start() {
        log.info("===> SyxRegistryCenter started.");
        executorService = Executors.newScheduledThreadPool(1);
    }

    @Override
    public void stop() {
        log.info("===> SyxRegistryCenter stopped.");
        gracefulShutdown(executorService);
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
    public void register(RpcServiceMeta service, InstanceMeta instance) {
        registryClient.register(service.identity(), convert(instance));
    }

    @Override
    public void unregister(RpcServiceMeta service, InstanceMeta instance) {
        registryClient.unregister(service.identity(), convert(instance));
    }

    @Override
    public List<InstanceMeta> fetchAll(RpcServiceMeta service) {
        List<RegistryInstanceMeta> instanceMetas = registryClient.fetchAll(service.identity());
        return instanceMetas.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public void subscribe(RpcServiceMeta service, RegistryChangeListener listener) {
        executorService.scheduleWithFixedDelay(() -> {
            // 获取当前服务版本
            Long version = VERSIONS.get(service.identity());
            if (Objects.isNull(version)) {
                version = -1L;
            }

            Long newVersion = registryClient.version(service.identity());
            log.info("===> SyxRegistryCenter subscribe service: {}, version: {}, newVersion: {}", service, version, newVersion);
            if (newVersion > version) {
                // 获取最新的服务实例
                List<InstanceMeta> nodes = fetchAll(service);
                listener.fire(new RegistryChangeEvent(nodes));
                VERSIONS.put(service.identity(), newVersion);
            }
        }, 1000, 5000, TimeUnit.MILLISECONDS);
    }

    private RegistryInstanceMeta convert(InstanceMeta instanceMeta) {
        RegistryInstanceMeta meta = new RegistryInstanceMeta();
        meta.setHost(instanceMeta.getHost());
        meta.setPort(instanceMeta.getPort());
        meta.setSchema(instanceMeta.getSchema());
        meta.setPath(instanceMeta.getPath());
        meta.setStatus(instanceMeta.isStatus());
        meta.setParameters(instanceMeta.getParameters());
        return meta;
    }

    private InstanceMeta convert(RegistryInstanceMeta instanceMeta) {
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
