package cn.syx.rpc.core.provider;

import cn.syx.rpc.core.annotation.SyxProvider;
import cn.syx.rpc.core.api.RpcRequest;
import cn.syx.rpc.core.api.RpcResponse;
import cn.syx.rpc.core.utils.TypeUtil;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        String methodSign = request.getMethodSign();

        System.out.println("consumer request ======> " + JSON.toJSONString(request));

        if (Objects.equals("toString", method)
                || Objects.equals("hashCode", method)
                || Objects.equals("equals", method)) {
            return null;
        }

        Object bean = PROVIDER_MAP.get(service);
        try {
            Method beanMethod = findMethod(bean, methodSign);
            List<Object> realArgs = new ArrayList<>();
            if (args != null && args.length > 0) {
                realArgs = Arrays.stream(args)
                        // fixme
                        .map(e -> TypeUtil.cast(e, beanMethod.getParameterTypes()[0]))
                        .collect(Collectors.toList());
            }
            Object data = beanMethod.invoke(bean, realArgs.toArray());
            return new RpcResponse<>(true, data, null);
        } catch (RuntimeException e) {
            return new RpcResponse<>(false, null, e);
        } catch (InvocationTargetException e) {
            return new RpcResponse<>(false, null, new RuntimeException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            return new RpcResponse<>(false, null, new RuntimeException(e.getMessage()));
        }
    }

    private Method findMethod(Object bean, String methodSign) {
        // 解析方法签名
        String[] split = methodSign.split("#");
        String methodNameKey = split[1];
        Method[] methods = bean.getClass().getMethods();
        for (Method method : methods) {
            StringBuilder sb = new StringBuilder().append(method.getName()).append("(");
            for (Class<?> parameterType : method.getParameterTypes()) {
                sb.append(parameterType.getCanonicalName()).append(",");
            }
            if (sb.charAt(sb.length() - 1) == ',') {
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.append(")");

            String currentMethodParamKey = sb.toString();
            if (Objects.equals(methodNameKey, currentMethodParamKey)) {
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
