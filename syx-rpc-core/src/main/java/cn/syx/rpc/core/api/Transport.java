package cn.syx.rpc.core.api;

public interface Transport {

    RpcResponse<?> invoker(RpcRequest request);
}
