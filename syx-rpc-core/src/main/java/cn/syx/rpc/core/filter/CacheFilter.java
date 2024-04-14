package cn.syx.rpc.core.filter;

import cn.syx.rpc.core.annotation.SyxFilter;
import cn.syx.rpc.core.api.Filter;
import cn.syx.rpc.core.api.RpcRequest;
import cn.syx.rpc.core.api.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@SyxFilter(name = "cache", order = 10000)
public class CacheFilter implements Filter {

    private static Map<String, Object> cache = new ConcurrentHashMap<>();

    @Override
    public Object preFilter(RpcRequest request) {
        Object re = cache.get(request.toString());
        if (re != null) {
            log.debug("===> cache hit: {}", request);
        }
        return re;
    }

    @Override
    public Object postFilter(RpcRequest request, RpcResponse<?> response, Object result) {
        cache.putIfAbsent(request.toString(), result);
        log.debug("===> cache set: {}", request);
        return result;
    }
}
