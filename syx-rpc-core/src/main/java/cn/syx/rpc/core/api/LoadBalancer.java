package cn.syx.rpc.core.api;

import cn.syx.rpc.core.meta.InstanceMeta;

import java.util.List;

public interface LoadBalancer<T> {

    T choose(List<T> providers);

    LoadBalancer<InstanceMeta> DEFAULT = providers -> {
        if (providers == null || providers.isEmpty()) {
            return null;
        }
        return providers.get(0);
    };
}
