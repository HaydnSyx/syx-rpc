package cn.syx.rpc.demo.provider;

import cn.syx.rpc.core.annotation.EnableProvider;
import cn.syx.rpc.core.config.ProviderConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@EnableProvider
@SpringBootApplication
public class SyxRpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SyxRpcDemoProviderApplication.class, args);
    }
}
