package cn.syx.rpc.demo.provider;

import cn.syx.rpc.core.api.RpcRequest;
import cn.syx.rpc.core.api.RpcResponse;
import cn.syx.rpc.core.provider.ProviderBootstrap;
import cn.syx.rpc.core.provider.ProviderConfig;
import cn.syx.rpc.core.provider.ProviderInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@Import({ProviderConfig.class})
public class SyxRpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SyxRpcDemoProviderApplication.class, args);
    }

    @Autowired
    private ProviderInvoker invoker;

    @RequestMapping("/")
    public RpcResponse<?> invoker(@RequestBody RpcRequest request) {
        return invoker.invokerRequest(request);
    }

    /*@Bean
    ApplicationRunner runner() {
        return args -> {
            RpcRequest req = new RpcRequest();
            req.setService("cn.syx.rpc.demo.api.UserService");
            req.setMethod("findById");
            req.setArgs(new Object[]{100});

            System.out.println("result: " + bootstrap.invokerRequest(req));
        };
    }*/
}
