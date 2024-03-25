package cn.syx.rpc.core.consumer;

import cn.syx.rpc.core.api.Filter;
import cn.syx.rpc.core.api.LoadBalancer;
import cn.syx.rpc.core.api.RegistryCenter;
import cn.syx.rpc.core.api.Router;
import cn.syx.rpc.core.cluster.RoundRibbonLoadBalancer;
import cn.syx.rpc.core.filter.CacheFilter;
import cn.syx.rpc.core.filter.MockFilter;
import cn.syx.rpc.core.meta.InstanceMeta;
import cn.syx.rpc.core.registry.zk.ZkRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

@Configuration
public class ConsumerConfig {

    @Bean
    public ConsumerBootstrap createConsumerBootstrap() {
        return new ConsumerBootstrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner consumer_runner_config(@Autowired ConsumerBootstrap consumerBootstrap) {
        return x -> consumerBootstrap.start();
    }

    /*@Bean
    public Filter defaultFilter() {
        return Filter.DEFAULT;
    }*/

    /*@Bean
    public Filter cacheFilter() {
        return new CacheFilter();
    }*/

    @Bean
    public Filter mockFilter() {
        return new MockFilter();
    }


    @Bean
    public LoadBalancer<InstanceMeta> loadBalancer() {
        return new RoundRibbonLoadBalancer<>();
    }

    @Bean
    public Router<InstanceMeta> router() {
        return Router.DEFAULT;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter registryCenter() {
        return new ZkRegistryCenter();
    }
}
