package cn.syx.rpc.core.provider;

import cn.syx.rpc.core.api.RegistryCenter;
import cn.syx.rpc.core.consumer.ConsumerBootstrap;
import cn.syx.rpc.core.registry.ZkRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ProviderConfig {

    @Bean
    public ProviderBootstrap providerBootstrap() {
        return new ProviderBootstrap();
    }

    @Bean(initMethod = "start")
    public RegistryCenter providerRegistryCenter() {
        return new ZkRegistryCenter();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner provider_runner_config(@Autowired ProviderBootstrap providerBootstrap) {
        return x -> providerBootstrap.start();
    }
}
