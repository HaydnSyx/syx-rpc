package cn.syx.rpc.core.consumer;

import cn.syx.rpc.core.annotation.SyxConsumer;
import cn.syx.rpc.core.api.*;
import cn.syx.rpc.core.config.AppProperties;
import cn.syx.rpc.core.config.ConsumerProperties;
import cn.syx.rpc.core.meta.InstanceMeta;
import cn.syx.rpc.core.meta.ServiceMeta;
import cn.syx.rpc.core.utils.MethodUtil;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class ConsumerBootstrap implements ApplicationContextAware {

    private ApplicationContext context;
    private final AppProperties appProperties;
    private final ConsumerProperties consumerProperties;

    private final Map<String, Object> STUB_MAP = new HashMap<>();

    @Override
    public void setApplicationContext(@NotNull ApplicationContext context) throws BeansException {
        this.context = context;
    }

    public ConsumerBootstrap(AppProperties appProperties, ConsumerProperties consumerProperties) {
        this.appProperties = appProperties;
        this.consumerProperties = consumerProperties;
    }

    public void start() {
        List<Filter> filters = context.getBeansOfType(Filter.class).values().stream().toList();
        Router<InstanceMeta> router = context.getBean(Router.class);
        LoadBalancer<InstanceMeta> loadBalancer = context.getBean(LoadBalancer.class);
        RegistryCenter registryCenter = context.getBean(RegistryCenter.class);
        RpcContext rpcContext = RpcContext.builder()
                .filters(filters)
                .router(router)
                .loadBalancer(loadBalancer)
                .build();
//        rpcContext.getParams().put("grayRatio", String.valueOf(grayRatio));

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
                .app(appProperties.getId())
                .namespace(appProperties.getNamespace())
                .env(appProperties.getEnv())
                .name(service.getCanonicalName())
                .version(appProperties.getVersion())
                .build();
        final List<InstanceMeta> providers = rc.fetchAll(serviceMeta);
        log.info("real providers ======> {}", JSON.toJSON(providers));
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
