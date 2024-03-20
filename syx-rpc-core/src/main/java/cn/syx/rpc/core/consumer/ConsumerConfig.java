package cn.syx.rpc.core.consumer;

import cn.syx.rpc.core.api.LoadBalancer;
import cn.syx.rpc.core.api.RegistryCenter;
import cn.syx.rpc.core.api.Router;
import cn.syx.rpc.core.cluster.RoundRibbonLoadBalancer;
import cn.syx.rpc.core.meta.InstanceMeta;
import cn.syx.rpc.core.registry.zk.ZkRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

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
