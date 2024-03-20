package cn.syx.rpc.core.consumer;

import cn.syx.rpc.core.annotation.SyxConsumer;
import cn.syx.rpc.core.api.LoadBalancer;
import cn.syx.rpc.core.api.RegistryCenter;
import cn.syx.rpc.core.api.Router;
import cn.syx.rpc.core.api.RpcContext;
import cn.syx.rpc.core.meta.InstanceMeta;
import cn.syx.rpc.core.meta.ServiceMeta;
import cn.syx.rpc.core.utils.MethodUtil;
import com.alibaba.fastjson2.JSON;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ConsumerBootstrap implements ApplicationContextAware {

    private ApplicationContext context;

    @Value("${app.id}")
    private String app;

    @Value("${app.namespace}")
    private String namespace;

    @Value("${app.env}")
    private String appEnv;

    @Value("${app.version}")
    private String version;

    private final Map<String, Object> STUB_MAP = new HashMap<>();

    @Override
    public void setApplicationContext(@NotNull ApplicationContext context) throws BeansException {
        this.context = context;
    }

    public void start() {
        Router<InstanceMeta> router = context.getBean(Router.class);
        LoadBalancer<InstanceMeta> loadBalancer = context.getBean(LoadBalancer.class);
        RegistryCenter registryCenter = context.getBean(RegistryCenter.class);
        RpcContext rpcContext = RpcContext.builder()
                .router(router)
                .loadBalancer(loadBalancer)
                .build();

        String[] names = context.getBeanDefinitionNames();
        for (String name : names) {
            Object bean = context.getBean(name);
            List<Field> fields = MethodUtil.findAnnotationField(bean.getClass(), SyxConsumer.class);

            fields.forEach(e -> {
                try {
                    Class<?> service = e.getType();
                    String serviceName = service.getCanonicalName();
                    Object consumer = STUB_MAP.get(serviceName);
                    if (Objects.isNull(consumer)) {
                        // 生成代理
                        consumer = createFromRegistryCenter(service, rpcContext, registryCenter);
                        STUB_MAP.put(serviceName, consumer);
                    }
                    e.setAccessible(true);
                    e.set(bean, consumer);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
    }

    private Object createFromRegistryCenter(Class<?> service, RpcContext context, RegistryCenter rc) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(app)
                .namespace(namespace)
                .env(appEnv)
                .name(service.getCanonicalName())
                .version(version)
                .build();
        final List<InstanceMeta> providers = rc.fetchAll(serviceMeta);
        System.out.println("real providers ======> " + JSON.toJSON(providers));
        // 订阅服务
        rc.subscribe(serviceMeta, event -> {
            providers.clear();
            providers.addAll(event.getData());
        });
        return createConsumer(service, context, providers);
    }

    private Object createConsumer(Class<?> service, RpcContext context, List<InstanceMeta> providerList) {
        return Proxy.newProxyInstance(
                service.getClassLoader(),
                new Class<?>[]{service}, new SyxInvokerHandler(service, context, providerList) {
                }
        );
    }
}
