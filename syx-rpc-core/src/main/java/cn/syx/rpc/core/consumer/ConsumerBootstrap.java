package cn.syx.rpc.core.consumer;

import cn.syx.rpc.core.annotation.SyxConsumer;
import cn.syx.rpc.core.annotation.SyxFilter;
import cn.syx.rpc.core.api.*;
import cn.syx.rpc.core.config.AppProperties;
import cn.syx.rpc.core.config.ConsumerProperties;
import cn.syx.rpc.core.meta.InstanceMeta;
import cn.syx.rpc.core.meta.ServiceMeta;
import cn.syx.rpc.core.utils.MethodUtil;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.*;

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
                .filterMap(convertFilters(filters))
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
                    SyxConsumer syx = e.getAnnotation(SyxConsumer.class);
                    String serviceName = service.getCanonicalName();
                    Object consumer = STUB_MAP.get(serviceName);
                    if (Objects.isNull(consumer)) {
                        // 生成代理
                        consumer = createFromRegistryCenter(service, rpcContext, registryCenter, syx);
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

    private Object createFromRegistryCenter(Class<?> service, RpcContext context, RegistryCenter rc, SyxConsumer syx) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .namespace(StringUtils.isBlank(syx.namespace()) ? appProperties.getNamespace() : syx.namespace())
                .env(appProperties.getEnv())
                .group(StringUtils.isBlank(syx.group()) ? appProperties.getGroup() : syx.group())
                .name(service.getCanonicalName())
                .version(StringUtils.isBlank(syx.version()) ? appProperties.getVersion() : syx.version())
                .build();
        final List<InstanceMeta> providers = rc.fetchAll(serviceMeta);
        log.info("real providers ======> {}", JSON.toJSON(providers));
        // 订阅服务
        rc.subscribe(serviceMeta, event -> {
            providers.clear();
            providers.addAll(event.getData());
        });

        RpcConsumerContext consumerContext = createConsumerContext(syx);

        return createConsumer(service, context, consumerContext, providers);
    }

    private Object createConsumer(Class<?> service, RpcContext context, RpcConsumerContext consumerContext, List<InstanceMeta> providerList) {
        return Proxy.newProxyInstance(
                service.getClassLoader(),
                new Class<?>[]{service}, new SyxInvokerHandler(service, context, consumerContext, providerList) {
                }
        );
    }

    private Map<String, WrapperFilter> convertFilters(List<Filter> filters) {
        Map<String, WrapperFilter> map = new HashMap<>();
        filters.forEach(e -> {
            SyxFilter annotation = e.getClass().getAnnotation(SyxFilter.class);
            String name = e.getClass().getCanonicalName();
            int order = -1;
            boolean global = true;
            if (annotation != null) {
                name = annotation.name();
                global = annotation.global();
                order = annotation.order();
            }
            WrapperFilter filter = WrapperFilter.builder()
                    .order(order)
                    .filter(e)
                    .global(global)
                    .build();
            map.put(name, filter);
        });
        return map;
    }

    private RpcConsumerContext createConsumerContext(SyxConsumer syx) {
        RpcConsumerContext consumerContext = new RpcConsumerContext();
        if (syx.timeout() > 0) {
            consumerContext.setTimeout(syx.timeout());
        } else {
            consumerContext.setTimeout(consumerProperties.getTimeout());
        }

        if (syx.retries() > 0) {
            consumerContext.setRetries(syx.retries());
        } else {
            consumerContext.setRetries(consumerProperties.getRetries());
        }

        consumerContext.setConnectionTimeout(consumerProperties.getConnectionTimeout());
        consumerContext.setFaultLimit(consumerProperties.getFaultLimit());
        consumerContext.setHalfOpenInitialDelay(consumerProperties.getHalfOpenInitialDelay());
        consumerContext.setHalfOpenDelay(consumerProperties.getHalfOpenDelay());
        consumerContext.setGrayRatio(consumerProperties.getGrayRatio());
        if (Objects.nonNull(syx.filters())) {
            consumerContext.setFilters(Arrays.asList(syx.filters()));
        }

        // 解析注解
        SyxConsumer.MethodCustomer[] customers = syx.methodCustomers();
        if (Objects.nonNull(customers)) {
            for (SyxConsumer.MethodCustomer customer : customers) {
                String methodName = customer.methodName();
                if (customer.timeout() > 0) {
                    consumerContext.getTimeoutMap().put(methodName, customer.timeout());
                }
                if (customer.retries() > 0) {
                    consumerContext.getRetrytMap().put(methodName, customer.retries());
                }
                if (Objects.nonNull(customer.filters())) {
                    consumerContext.getFilterMap().put(methodName, Arrays.asList(customer.filters()));
                }
            }
        }

        return consumerContext;
    }
}
