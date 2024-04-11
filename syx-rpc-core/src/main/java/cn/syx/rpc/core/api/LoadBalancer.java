package cn.syx.rpc.core.api;

import cn.syx.rpc.core.meta.InstanceMeta;

import java.util.List;

public interface LoadBalancer<T> {

    T choose(List<T> providers);
}
