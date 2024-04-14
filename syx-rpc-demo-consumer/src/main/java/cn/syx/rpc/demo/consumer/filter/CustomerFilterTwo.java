package cn.syx.rpc.demo.consumer.filter;

import cn.syx.rpc.core.annotation.SyxFilter;
import cn.syx.rpc.core.api.Filter;
import cn.syx.rpc.core.api.RpcRequest;
import cn.syx.rpc.core.api.RpcResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@SyxFilter(name = "customerFilter2", order = 2)
public class CustomerFilterTwo implements Filter {
    @Override
    public Object preFilter(RpcRequest request) {
        log.info("===> In CustomerFilterTwo preFilter");
        return null;
    }

    @Override
    public Object postFilter(RpcRequest request, RpcResponse<?> response, Object result) {
        log.info("===> In CustomerFilterTwo postFilter");
        return result;
    }
}
