package cn.syx.rpc.core.provider;

import cn.syx.rpc.core.api.RpcRequest;
import cn.syx.rpc.core.api.RpcResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpringBootTransport {

    @Autowired
    private ProviderInvoker invoker;

    @RequestMapping("/rpc")
    public RpcResponse<?> doInvoker(@RequestBody RpcRequest request) {
        return invoker.invokerRequest(request);
    }
}
