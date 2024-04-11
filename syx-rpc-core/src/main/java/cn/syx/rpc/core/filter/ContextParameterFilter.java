package cn.syx.rpc.core.filter;

import cn.syx.rpc.core.api.Filter;
import cn.syx.rpc.core.api.RpcContext;
import cn.syx.rpc.core.api.RpcRequest;
import cn.syx.rpc.core.api.RpcResponse;

import java.util.Map;

/**
 * 处理上下文参数.
 */
public class ContextParameterFilter implements Filter {

    @Override
    public Object preFilter(RpcRequest request) {
        Map<String, String> params = RpcContext.ContextParams.get();
        if (!params.isEmpty()) {
            request.getParams().putAll(params);
        }
        return null;
    }

    @Override
    public Object postFilter(RpcRequest request, RpcResponse<?> response, Object result) {
        RpcContext.ContextParams.get().clear();
        return result;
    }
}
