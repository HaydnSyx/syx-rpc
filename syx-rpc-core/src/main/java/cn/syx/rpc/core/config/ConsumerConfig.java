package cn.syx.rpc.core.config;

import cn.syx.rpc.core.api.Filter;
import cn.syx.rpc.core.api.LoadBalancer;
import cn.syx.rpc.core.api.RegistryCenter;
import cn.syx.rpc.core.api.Router;
import cn.syx.rpc.core.cluster.GrayRouter;
import cn.syx.rpc.core.cluster.RoundRibbonLoadBalancer;
import cn.syx.rpc.core.consumer.ConsumerBootstrap;
import cn.syx.rpc.core.filter.ContextParameterFilter;
import cn.syx.rpc.core.meta.InstanceMeta;
import cn.syx.rpc.core.registry.zk.ZkRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
    public ConsumerBootstrap createConsumerBootstrap() {
        return new ConsumerBootstrap(appProperties, consumerProperties);
    }

    @Bean
    @Order(Integer.MIN_VALUE + 1)
    public ApplicationRunner consumer_runner_config(@Autowired ConsumerBootstrap consumerBootstrap) {
        return x -> consumerBootstrap.start();
    }

    @Bean
    public Filter defaultFilter() {
        return new ContextParameterFilter();
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
        return new ZkRegistryCenter();
    }
}
