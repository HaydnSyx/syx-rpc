package cn.syx.rpc.core.consumer;

import cn.syx.rpc.core.api.RpcRequest;
import cn.syx.rpc.core.api.RpcResponse;

public interface HttpInvoker {

    RpcResponse<?> post(RpcRequest request, String url);
}
