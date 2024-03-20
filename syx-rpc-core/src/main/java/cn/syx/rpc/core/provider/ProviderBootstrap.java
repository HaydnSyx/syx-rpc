package cn.syx.rpc.core.provider;

import cn.syx.rpc.core.annotation.SyxProvider;
import cn.syx.rpc.core.api.RegistryCenter;
import cn.syx.rpc.core.api.RpcRequest;
import cn.syx.rpc.core.api.RpcResponse;
import cn.syx.rpc.core.meta.ProviderMeta;
import cn.syx.rpc.core.utils.MethodUtil;
import cn.syx.rpc.core.utils.TypeUtil;
import com.alibaba.fastjson2.JSON;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProviderBootstrap implements ApplicationContextAware {

    private ApplicationContext context;

    private RegistryCenter registryCenter;

    private String instance;
    @Value("${server.port}")
    private int port;

    private MultiValueMap<String, ProviderMeta> skeletonMap = new LinkedMultiValueMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }


    public MultiValueMap<String, ProviderMeta> getSkeletonMap() {
        return skeletonMap;
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
        this.instance = ip + "_" + port;
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
        RegistryCenter registryCenter = context.getBean(RegistryCenter.class);
        registryCenter.register(service, this.instance);
    }

    private void unregisterService(String service) {
        RegistryCenter registryCenter = context.getBean(RegistryCenter.class);
        registryCenter.unregister(service, this.instance);
    }
}
