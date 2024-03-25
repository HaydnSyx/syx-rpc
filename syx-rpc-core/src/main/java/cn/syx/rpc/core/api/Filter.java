package cn.syx.rpc.core.api;

public interface Filter {

    Object preFilter(RpcRequest request);

    Object postFilter(RpcRequest request, RpcResponse<?> response, Object result);

//    Filter next();
}
