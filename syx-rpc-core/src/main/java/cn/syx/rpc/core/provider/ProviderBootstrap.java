package cn.syx.rpc.core.provider;

import cn.syx.rpc.core.annotation.SyxProvider;
import cn.syx.rpc.core.api.RegistryCenter;
import cn.syx.rpc.core.meta.InstanceMeta;
import cn.syx.rpc.core.meta.ProviderMeta;
import cn.syx.rpc.core.meta.ServiceMeta;
import cn.syx.rpc.core.utils.MethodUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Map;

public class ProviderBootstrap implements ApplicationContextAware {

    private ApplicationContext context;

    private RegistryCenter registryCenter;

    private InstanceMeta instance;
    @Value("${server.port}")
    private int port;

    @Value("${app.id}")
    private String app;

    @Value("${app.namespace}")
    private String namespace;

    @Value("${app.env}")
    private String env;

    @Value("${app.version}")
    private String version;

    @Getter
    private final MultiValueMap<String, ProviderMeta> skeletonMap = new LinkedMultiValueMap<>();

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @PostConstruct
    public void init() {
        Map<String, Object> beans = context.getBeansWithAnnotation(SyxProvider.class);
        this.registryCenter = context.getBean(RegistryCenter.class);
        beans.values().forEach(this::getInterface);
    }

    @SneakyThrows
    public void start() {
        String ip = InetAddress.getLocalHost().getHostAddress();
        this.instance = InstanceMeta.http(ip, port);
        this.registryCenter.start();
        skeletonMap.keySet().forEach(this::registerService);
    }

    @PreDestroy
    public void stop() {
        skeletonMap.keySet().forEach(this::unregisterService);
        this.registryCenter.stop();
    }

    private void getInterface(Object o) {
        Class<?> cls = o.getClass().getInterfaces()[0];
        Method[] methods = cls.getMethods();
        for (Method method : methods) {
            if (MethodUtil.isLocalMethod(method)) {
                continue;
            }

            createProviderMeta(cls, o, method);
        }
    }

    private void createProviderMeta(Class<?> cls, Object service, Method method) {
        ProviderMeta providerMeta = new ProviderMeta();
        providerMeta.setMethod(method);
        providerMeta.setMethodSign(MethodUtil.generateMethodSign(method));
        providerMeta.setService(service);
        skeletonMap.add(cls.getCanonicalName(), providerMeta);
    }

    private void registerService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(app)
                .namespace(namespace)
                .env(env)
                .name(service)
                .version(version)
                .build();
        this.registryCenter.register(serviceMeta, this.instance);
    }

    private void unregisterService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(app)
                .namespace(namespace)
                .env(env)
                .name(service)
                .version(version)
                .build();
        this.registryCenter.unregister(serviceMeta, this.instance);
    }
}
