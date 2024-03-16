package cn.syx.rpc.core.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcContext {

    private List<Filter> filters;
    private Router router;
    private LoadBalancer loadBalancer;
}
