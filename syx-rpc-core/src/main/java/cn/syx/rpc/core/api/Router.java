package cn.syx.rpc.core.api;

import cn.syx.rpc.core.meta.InstanceMeta;

import java.util.List;

public interface Router<T> {

    List<T> route(List<T> providers);
}
