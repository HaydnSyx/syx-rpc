package cn.syx.rpc.core.config;

import cn.syx.rpc.core.api.*;
import cn.syx.rpc.core.cluster.GrayRouter;
import cn.syx.rpc.core.cluster.RoundRibbonLoadBalancer;
import cn.syx.rpc.core.configcenter.DynamicRequestTime;
import cn.syx.rpc.core.configcenter.apollo.ApolloTimeoutChangedListener;
import cn.syx.rpc.core.consumer.ConsumerBootstrap;
import cn.syx.rpc.core.filter.CacheFilter;
import cn.syx.rpc.core.filter.ContextParameterFilter;
import cn.syx.rpc.core.filter.MockFilter;
import cn.syx.rpc.core.meta.InstanceMeta;
import cn.syx.rpc.core.registry.syx.SyxRegistryCenter;
import cn.syx.rpc.core.serialize.SyxConsumerSerializer;
import cn.syx.rpc.core.transport.CommonHttpTransporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

@Configuration
@Import({AppProperties.class, ConsumerProperties.class})
public class ConsumerConfig {

    @Autowired
    private AppProperties appProperties;
    @Autowired
    private ConsumerProperties consumerProperties;

    @Bean
    @ConditionalOnProperty(prefix = "apollo.bootstrap", value = "enabled")
    public ApolloTimeoutChangedListener consumerTimeoutChangeListener() {
        return new ApolloTimeoutChangedListener();
    }

    @Bean
    public ConsumerBootstrap createConsumerBootstrap() {
        return new ConsumerBootstrap(appProperties, consumerProperties);
    }

    @Bean
    @Order(Integer.MIN_VALUE + 1)
    public ApplicationRunner consumer_runner_config(@Autowired ConsumerBootstrap consumerBootstrap) {
        return x -> consumerBootstrap.start();
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicRequestTime dynamicRequestTime() {
        return new ApolloTimeoutChangedListener();
    }

    @Bean
    @ConditionalOnMissingBean
    public Transporter transporter(@Autowired DynamicRequestTime requestTime) {
        return new CommonHttpTransporter(requestTime);
    }

    @Bean
    public Filter defaultFilter() {
        return new ContextParameterFilter();
    }

    @Bean
    @ConditionalOnProperty(prefix = "cn.syx.rpc.core.api.filter.cache", value = "enabled")
    public Filter cacheFilter() {
        return new CacheFilter();
    }

    @Bean
    @ConditionalOnProperty(prefix = "cn.syx.rpc.core.api.filter.mock", value = "enabled")
    public Filter mockFilter() {
        return new MockFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public ConsumerSerializer consumerSerializer() {
        return new SyxConsumerSerializer();
    }

    @Bean
    public LoadBalancer<InstanceMeta> loadBalancer() {
        return new RoundRibbonLoadBalancer<>();
    }

    @Bean
    public Router<InstanceMeta> router() {
        return new GrayRouter(consumerProperties.getGrayRatio());
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public RegistryCenter registryCenter() {
        return new SyxRegistryCenter();
    }
}
