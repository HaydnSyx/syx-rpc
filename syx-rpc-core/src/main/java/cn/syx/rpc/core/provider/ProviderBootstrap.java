package cn.syx.rpc.core.provider;

import cn.syx.rpc.core.annotation.SyxProvider;
import cn.syx.rpc.core.api.RpcRequest;
import cn.syx.rpc.core.api.RpcResponse;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProviderBootstrap implements ApplicationContextAware {

    private ApplicationContext context;

    private Map<String, Object> PROVIDER_MAP = new HashMap<>();

    @PostConstruct
    public void buildProvider() {
        Map<String, Object> beans = context.getBeansWithAnnotation(SyxProvider.class);
        beans.values().forEach(this::getInterface);
    }

    public RpcResponse<Object> invokerRequest(RpcRequest request) {
        String service = request.getService();
        String method = request.getMethod();
        Object[] args = request.getArgs();

        Object bean = PROVIDER_MAP.get(service);
        try {
            Method beanMethod = findMethod(bean, method);
            Object data = beanMethod.invoke(bean, args);
            return new RpcResponse<>(true, data);
        } catch (NullPointerException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Method findMethod(Object bean, String targetMethodName) {
        Method[] methods = bean.getClass().getMethods();
        for (Method method : methods) {
            if (Objects.equals(method.getName(), targetMethodName)) {
                return method;
            }
        }
        return null;
    }

    private void getInterface(Object o) {
        Class<?> cls = o.getClass().getInterfaces()[0];
        PROVIDER_MAP.put(cls.getCanonicalName(), o);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
