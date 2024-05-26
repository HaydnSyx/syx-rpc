package cn.syx.rpc.core.api;

import cn.syx.registry.core.model.instance.RpcServiceMeta;
import cn.syx.rpc.core.meta.InstanceMeta;
import cn.syx.rpc.core.registry.RegistryChangeListener;

import java.util.List;

public interface RegistryCenter {

    void start();

    void stop();

    void register(RpcServiceMeta service, InstanceMeta instance);

    void unregister(RpcServiceMeta service, InstanceMeta instance);

    List<InstanceMeta> fetchAll(RpcServiceMeta service);

    void subscribe(RpcServiceMeta service, RegistryChangeListener listener);
}
