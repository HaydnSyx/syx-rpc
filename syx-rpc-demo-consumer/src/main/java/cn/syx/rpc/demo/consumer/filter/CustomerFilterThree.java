package cn.syx.rpc.demo.consumer.filter;

import cn.syx.rpc.core.annotation.SyxFilter;
import cn.syx.rpc.core.api.Filter;
import cn.syx.rpc.core.api.RpcRequest;
import cn.syx.rpc.core.api.RpcResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@SyxFilter(name = "customerFilter3", order = 3)
public class CustomerFilterThree implements Filter {
    @Override
    public Object preFilter(RpcRequest request) {
        log.info("===> In CustomerFilterThree preFilter");
        return null;
    }

    @Override
    public Object postFilter(RpcRequest request, RpcResponse<?> response, Object result) {
        log.info("===> In CustomerFilterThree postFilter");
        return result;
    }
}
