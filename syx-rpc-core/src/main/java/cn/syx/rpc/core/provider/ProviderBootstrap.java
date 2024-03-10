package cn.syx.rpc.core.provider;

import cn.syx.rpc.core.annotation.SyxProvider;
import cn.syx.rpc.core.api.RpcRequest;
import cn.syx.rpc.core.api.RpcResponse;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.PostConstruct;
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @PostConstruct
    public void buildProvider() {
        Map<String, Object> beans = context.getBeansWithAnnotation(SyxProvider.class);
        beans.values().forEach(this::getInterface);
    }

    public RpcResponse<Object> invokerRequest(RpcRequest request) {
        String service = request.getService();
        String method = request.getMethod();
        Object[] args = request.getArgs();

        System.out.println("consumer request ======> " + JSON.toJSONString(request));

        if (Objects.equals("toString", method)
                || Objects.equals("hashCode", method)
                || Objects.equals("equals", method)) {
            return null;
        }

        Object bean = PROVIDER_MAP.get(service);
        try {
            Method beanMethod = findMethod(bean, method);
            Object data = beanMethod.invoke(bean, args);
            return new RpcResponse<>(true, data, null);
        } catch (RuntimeException e) {
            return new RpcResponse<>(false, null, e);
        } catch (InvocationTargetException e) {
            return new RpcResponse<>(false, null, new RuntimeException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            return new RpcResponse<>(false, null, new RuntimeException(e.getMessage()));
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
}
