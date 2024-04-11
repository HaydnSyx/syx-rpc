package cn.syx.rpc.core.provider;

import cn.syx.rpc.core.annotation.SyxProvider;
import cn.syx.rpc.core.api.RegistryCenter;
import cn.syx.rpc.core.config.AppProperties;
import cn.syx.rpc.core.config.ProviderProperties;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Map;

public class ProviderBootstrap implements ApplicationContextAware {

    /** 配置类相关信息 */
    private ApplicationContext context;
    private final AppProperties appProperties;
    private final ProviderProperties providerProperties;

    /** 组件类信息 */
    private RegistryCenter registryCenter;

    /** 服务实例信息 */
    private InstanceMeta instance;
    @Getter
    private final MultiValueMap<String, ProviderMeta> skeletonMap = new LinkedMultiValueMap<>();

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    public ProviderBootstrap(AppProperties appProperties,
                             ProviderProperties providerProperties) {
        this.appProperties = appProperties;
        this.providerProperties = providerProperties;
    }

    /**
     * 项目启动时，扫描所有的带有@SyxProvider注解的类，获取类的接口和方法
     * 并将接口和方法封装成ProviderMeta对象，放入skeletonMap中
     * 数据在本地内存中
     */
    @PostConstruct
    public void init() {
        Map<String, Object> beans = context.getBeansWithAnnotation(SyxProvider.class);
        this.registryCenter = context.getBean(RegistryCenter.class);
        beans.values().forEach(this::getInterface);
    }

    /**
     * 项目启动后执行该方法
     * 扫描skeletonMap中的数据，将数据注册到注册中心
     */
    @SneakyThrows
    public void start() {
        String ip = InetAddress.getLocalHost().getHostAddress();
        this.instance = InstanceMeta.http(ip, providerProperties.getPort());
        this.instance.getParameters().putAll(providerProperties.getMetas());
        this.registryCenter.start();
        skeletonMap.keySet().forEach(this::registerService);
    }

    /**
     * 项目关闭时，执行该方法
     * 需要注意执行顺序，先注销服务，再关闭注册中心
     */
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
                .app(appProperties.getId())
                .namespace(appProperties.getNamespace())
                .env(appProperties.getEnv())
                .name(service)
                .version(appProperties.getVersion())
                .build();
        this.registryCenter.register(serviceMeta, this.instance);
    }

    private void unregisterService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(appProperties.getId())
                .namespace(appProperties.getNamespace())
                .env(appProperties.getEnv())
                .name(service)
                .version(appProperties.getVersion())
                .build();
        this.registryCenter.unregister(serviceMeta, this.instance);
    }
}
