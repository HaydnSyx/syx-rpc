package cn.syx.rpc.core.provider;

import cn.syx.rpc.core.api.RpcRequest;
import cn.syx.rpc.core.api.RpcResponse;
import cn.syx.rpc.core.meta.ProviderMeta;
import cn.syx.rpc.core.utils.MethodUtil;
import cn.syx.rpc.core.utils.TypeUtil;
import com.alibaba.fastjson2.JSON;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

public class ProviderInvoker {

    private final MultiValueMap<String, ProviderMeta> PROVIDER_MAP;

    public ProviderInvoker(ProviderBootstrap bootstrap) {
        this.PROVIDER_MAP = bootstrap.getSkeletonMap();
    }

    public RpcResponse<Object> invokerRequest(RpcRequest request) {
        String service = request.getService();
        Object[] args = request.getArgs();
        String methodSign = request.getMethodSign();

        System.out.println("consumer request ======> " + JSON.toJSONString(request));

        String method = methodSign.split("\\(")[0];
        if (MethodUtil.isLocalMethod(method)) {
            return null;
        }

        List<ProviderMeta> metas = PROVIDER_MAP.get(service);
        try {
            if (Objects.isNull(metas) || metas.isEmpty()) {
                throw new RuntimeException("未发现提供方");
            }

            ProviderMeta providerMeta= findProviderMeta(metas, methodSign);
            if (Objects.isNull(providerMeta)) {
                throw new RuntimeException("未匹配到合适方法");
            }

            Method metaMethod = providerMeta.getMethod();
            castArgs(args, metaMethod);
            Object data = metaMethod.invoke(providerMeta.getService(), args);
            return new RpcResponse<>(true, data, null);
        } catch (RuntimeException e) {
            return new RpcResponse<>(false, null, e);
        } catch (InvocationTargetException e) {
            return new RpcResponse<>(false, null, new RuntimeException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            return new RpcResponse<>(false, null, new RuntimeException(e.getMessage()));
        }
    }

    private static void castArgs(Object[] args, Method metaMethod) {
        if (args != null && args.length > 0) {
            Type[] parameterTypes = metaMethod.getGenericParameterTypes();
            for (int i = 0; i < args.length; i++) {
                Object cast = TypeUtil.castV1(args[i], parameterTypes[i]);
                args[i] = cast;
            }
        }
    }

    private ProviderMeta findProviderMeta(List<ProviderMeta> metas, String methodSign) {
        for (ProviderMeta meta : metas) {
            if (meta.getMethodSign().equals(methodSign)) {
                return meta;
            }
        }
        return null;
    }
}
