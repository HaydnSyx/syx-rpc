package cn.syx.rpc.core.config;

import cn.syx.rpc.core.api.RegistryCenter;
import cn.syx.rpc.core.configcenter.apollo.ApolloChangedListener;
import cn.syx.rpc.core.provider.ProviderBootstrap;
import cn.syx.rpc.core.provider.ProviderInvoker;
import cn.syx.rpc.core.registry.syx.SyxRegistryCenter;
import cn.syx.rpc.core.provider.ProviderExposer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

@Configuration
@Import({AppProperties.class, ProviderProperties.class, ProviderExposer.class})
public class ProviderConfig {

    @Value("${server.port:8080}")
    private int port;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "apollo.bootstrap", value = "enabled")
    ApolloChangedListener providerApolloChangedListener() {
        return new ApolloChangedListener();
    }

    @Bean
    public ProviderBootstrap providerBootstrap(
            @Autowired AppProperties appProperties,
            @Autowired ProviderProperties providerProperties) {
        if (providerProperties.getPort() <= 0) {
            providerProperties.setPort(port);
        }
        return new ProviderBootstrap(appProperties, providerProperties);
    }

    @Bean
    public ProviderInvoker providerInvoker(@Autowired ProviderBootstrap providerBootstrap) {
        return new ProviderInvoker(providerBootstrap);
    }

    @Bean
    @ConditionalOnMissingBean
    public RegistryCenter providerRegistryCenter() {
        return new SyxRegistryCenter();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner provider_runner_config(@Autowired ProviderBootstrap providerBootstrap) {
        return x -> providerBootstrap.start();
    }
}
