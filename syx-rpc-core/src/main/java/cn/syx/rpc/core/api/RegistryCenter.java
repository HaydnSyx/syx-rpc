package cn.syx.rpc.core.api;

import cn.syx.rpc.core.meta.InstanceMeta;
import cn.syx.rpc.core.meta.ServiceMeta;
import cn.syx.rpc.core.registry.ChangeListener;

import java.util.List;

public interface RegistryCenter {

    void start();

    void stop();

    void register(ServiceMeta service, InstanceMeta instance);

    void unregister(ServiceMeta service, InstanceMeta instance);

    List<InstanceMeta> fetchAll(ServiceMeta service);

    void subscribe(ServiceMeta service, ChangeListener listener);
}
