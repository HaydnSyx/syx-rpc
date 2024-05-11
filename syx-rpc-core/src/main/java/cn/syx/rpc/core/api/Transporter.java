package cn.syx.rpc.core.api;

public interface Transporter {

    RpcResponse<?> invoke(byte[] request, String url);
}
