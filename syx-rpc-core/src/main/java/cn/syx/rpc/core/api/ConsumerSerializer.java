package cn.syx.rpc.core.api;

import java.lang.reflect.Type;

public interface ConsumerSerializer {

    byte[] serialize(RpcRequest obj);

    Object deserialize(RpcResponse<?> response, Type type);
}
