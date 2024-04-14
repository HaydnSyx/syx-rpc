package cn.syx.rpc.core.filter;

import cn.syx.rpc.core.api.Filter;
import cn.syx.rpc.core.api.RpcRequest;
import cn.syx.rpc.core.api.RpcResponse;
import cn.syx.rpc.core.api.WrapperFilter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@Getter
public class FilterChain implements Filter {

    private final List<Filter> filters = new ArrayList<>();

    public static FilterChain create(Map<String, WrapperFilter> filterMap, List<String> filterNames) {
        FilterChain chain = new FilterChain();

        Set<String> names = new HashSet<>();
        if (Objects.nonNull(filterNames) && !filterNames.isEmpty()) {
            names.addAll(filterNames);
        }
        // 没有设置则添加所有全局过滤器
        else {
            filterMap.forEach((k, v) -> {
                if (v.isGlobal()) {
                    names.add(k);
                }
            });
        }

        // 如果没有配置default，则默认添加default
        names.add("default");

        List<WrapperFilter> wrapperFilters = filterMap.entrySet().stream()
                .filter(e -> names.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .sorted(Comparator.comparing(WrapperFilter::getOrder))
                .toList();

        List<Filter> result = wrapperFilters.stream()
                .map(WrapperFilter::getFilter)
                .toList();

        chain.getFilters().addAll(result);

        return chain;
    }

    @Override
    public Object preFilter(RpcRequest request) {
        for (int i = 0; i < filters.size(); i++) {
            Object re = filters.get(i).preFilter(request);
            if (Objects.nonNull(re)) {
                return re;
            }
        }
        return null;
    }

    @Override
    public Object postFilter(RpcRequest request, RpcResponse<?> response, Object result) {
        for (int i = filters.size() - 1; i >= 0; i--) {
            result = filters.get(i).postFilter(request, response, result);
        }
        return result;
    }
}
