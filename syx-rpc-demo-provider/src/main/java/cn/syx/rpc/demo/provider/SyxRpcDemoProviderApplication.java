package cn.syx.rpc.demo.provider;

import cn.syx.config.client.annotation.EnableSyxConfig;
import cn.syx.rpc.core.annotation.EnableProvider;
import cn.syx.rpc.core.config.ProviderConfig;
import cn.syx.rpc.core.config.ProviderProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableProvider
//@EnableSyxConfig
@SpringBootApplication
public class SyxRpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SyxRpcDemoProviderApplication.class, args);
    }

    @Autowired
    private ProviderProperties providerProperties;

    @RequestMapping("/metas")
    public String meta() {
        System.out.println(System.identityHashCode(providerProperties.getMetas()));
        return providerProperties.getMetas().toString();
    }
}
