package cn.syx.rpc.demo.consumer;

import cn.syx.rpc.core.annotation.SyxConsumer;
import cn.syx.rpc.core.consumer.ConsumerConfig;
import cn.syx.rpc.demo.api.DemoService;
import cn.syx.rpc.demo.api.OrderService;
import cn.syx.rpc.demo.api.User;
import cn.syx.rpc.demo.api.UserService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

@SpringBootApplication
@Import({ConsumerConfig.class})
public class SyxRpcDemoConsumerApplication {

    @SyxConsumer
    private UserService userService;
    @SyxConsumer
    private OrderService orderService;
    @SyxConsumer
    private DemoService demoService;

    public static void main(String[] args) {
        SpringApplication.run(SyxRpcDemoConsumerApplication.class, args);
    }


    @Bean
    @Order(Integer.MAX_VALUE)
    public ApplicationRunner consumer_runner() {
        return x -> {
            User user = userService.findById(100);
            System.out.println("getUser(100) =====> " + user);

            int num = demoService.aaa(10);
            System.out.println("aaa(10) =====> " + num);

            demoService.bbb("syx");

//            cn.syx.rpc.demo.api.Order order = orderService.findById(1L);
//            System.out.println("getOrder(1) =====> " + order);

//            order = orderService.findById(404L);
//            System.out.println("getOrder(404) =====> " + order);
        };
    }
}
