package cn.syx.rpc.core.serialize;

import cn.syx.rpc.core.api.ConsumerSerializer;
import cn.syx.rpc.core.api.RpcException;
import cn.syx.rpc.core.api.RpcRequest;
import cn.syx.rpc.core.api.RpcResponse;
import cn.syx.rpc.core.utils.TypeUtil;
import com.alibaba.fastjson2.JSON;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class SyxConsumerSerializer implements ConsumerSerializer {

    @Override
    public byte[] serialize(RpcRequest req) {
        return JSON.toJSONString(req).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Object deserialize(RpcResponse<?> response, Type type) {
        if (Objects.isNull(response)) {
            return null;
        }

        if (response.isStatus()) {
            Object data = response.getData();
            return TypeUtil.castV1(data, type);
        }

        throw new RpcException(response.getEx());
    }
}
