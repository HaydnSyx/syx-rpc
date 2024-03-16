package cn.syx.rpc.core.api;

import java.util.List;

public interface LoadBalancer<T> {

    T choose(List<T> providers);

    LoadBalancer DEFAULT = providers -> {
        if (providers == null || providers.isEmpty()) {
            return null;
        }
        return providers.get(0);
    };
}
