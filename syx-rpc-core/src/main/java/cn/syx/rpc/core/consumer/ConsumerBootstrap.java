package cn.syx.rpc.core.consumer;

import cn.syx.rpc.core.annotation.SyxConsumer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Collectors;

public class ConsumerBootstrap implements ApplicationContextAware {

    private ApplicationContext context;

    private Map<String, Object> STUB_MAP = new HashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    public void start() {
        String[] names = context.getBeanDefinitionNames();
        for (String name : names) {
            Object bean = context.getBean(name);
            List<Field> fields = findAnnotationField(bean.getClass(), SyxConsumer.class);

            fields.forEach(e -> {
                try {
                    Class<?> service = e.getType();
                    String serviceName = service.getCanonicalName();
                    Object consumer = STUB_MAP.get(serviceName);
                    if (Objects.isNull(consumer)) {
                        // 生成代理
                        consumer = createConsumer(service);

                        STUB_MAP.put(serviceName, consumer);
                    }
                    e.setAccessible(true);
                    e.set(bean, consumer);
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            });
        }
    }

    public List<Field> findAnnotationField(Class<?> cls, Class<? extends Annotation> annotationCls) {
        List<Field> result = new ArrayList<>();
        while (cls != null) {
            Field[] fields = cls.getDeclaredFields();
            if (fields.length == 0) {
                cls = cls.getSuperclass();
                continue;
            }

            result.addAll(Arrays.stream(fields)
                    .filter(e -> e.isAnnotationPresent(annotationCls))
                    .toList());
            cls = cls.getSuperclass();
        }

        return result;
    }

    private Object createConsumer(Class<?> service) {
        return Proxy.newProxyInstance(
                service.getClassLoader(),
                new Class<?>[]{service}, new SyxInvokerHandler(service) {
                }
        );
    }
}
