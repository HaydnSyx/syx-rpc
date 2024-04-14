package cn.syx.rpc.core.api;

import cn.syx.rpc.core.meta.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcContext {

    private Map<String, WrapperFilter> filterMap;
    private Router<InstanceMeta> router;
    private LoadBalancer<InstanceMeta> loadBalancer;
    private Map<String, String> params = new HashMap<>();

    public static ThreadLocal<Map<String, String>> ContextParams = ThreadLocal.withInitial(
            HashMap::new
    );

    public static void setContextParameter(String key, String value) {
        ContextParams.get().put(key, value);
    }

    public static String getContextParameter(String key) {
        return ContextParams.get().get(key);
    }

    public static void removeContextParameter(String key) {
        ContextParams.get().remove(key);
    }
}
