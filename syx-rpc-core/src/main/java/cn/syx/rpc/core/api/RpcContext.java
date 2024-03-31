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

    private List<Filter> filters;
    private Router<InstanceMeta> router;
    private LoadBalancer<InstanceMeta> loadBalancer;
    private Map<String, String> params = new HashMap<>();
}
