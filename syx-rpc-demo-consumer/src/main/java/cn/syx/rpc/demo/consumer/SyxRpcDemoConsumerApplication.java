package cn.syx.rpc.demo.consumer;

import cn.syx.rpc.core.annotation.SyxConsumer;
import cn.syx.rpc.core.api.RpcRequest;
import cn.syx.rpc.core.api.RpcResponse;
import cn.syx.rpc.core.consumer.ConsumerConfig;
import cn.syx.rpc.demo.api.DemoService;
import cn.syx.rpc.demo.api.OrderService;
import cn.syx.rpc.demo.api.User;
import cn.syx.rpc.demo.api.UserService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
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

    @GetMapping("/")
    public User findById(@RequestParam("id") int id) {
        return userService.findById(id);
    }

    @Bean
    @Order(Integer.MAX_VALUE)
    public ApplicationRunner consumer_runner() {
        return x -> {
            User user = userService.findById(100);
            log.info("getUser(100) =====> " + user);

            /*int num = demoService.aaa();
            log.info("aaa() =====> " + num);

            num = demoService.aaa(10);
            log.info("aaa(int) =====> " + num);

            num = demoService.aaa(Integer.valueOf(9999));
            log.info("aaa(Integer) =====> " + num);

            num = demoService.aaa(888, "syx");
            log.info("aaa(int, String) =====> " + num);

            User user1 = new User(999, "syx");
            int num4 = demoService.aaa(user1);
            log.info("aaa(user) =====> " + num4);

            long result = demoService.aaa(100L);
            log.info("aaa(long) =====> " + result);

            demoService.bbb("syx");

            cn.syx.rpc.demo.api.Order order = orderService.findById(1L);
            log.info("getOrder(1) =====> " + order);

//            order = orderService.findById(404L);
//            log.info("getOrder(404) =====> " + order);

            User user2 = userService.findById(100);
            log.info("findById(100) =====> " + user2);

            user2.setId(200);
            int id = userService.parseUser(user);
            log.info("parseUser(user) =====> " + id);


            Map<String, Object> map = userService.findByIdWithMap(300);
            log.info("findByIdWithMap(300) =====> " + JSON.toJSONString(map));
            User user3 = userService.convertTOUser(Map.of("id", 200, "name", "syx"));
            log.info("convertTOUser(map) =====> " + user3);

            int[] jjj = demoService.jjj(100);
            log.info("jjj(100) =====> " + JSON.toJSONString(jjj));

            long[] kkk = demoService.kkk();
            log.info("kkk() =====> " + JSON.toJSONString(kkk));

//            int[] mmm = demoService.mmm(new int[]{1, 1, 2, 2, 3, 3});
//            log.info("mmm([1,1,2,2,3,3]) =====> " + JSON.toJSONString(mmm));

            List<User> userList1 = Arrays.asList(
                    new User(1, "name1"),
                    new User(2, "name2"),
                    new User(3, "name3")
            );
            List<User> userList2 = userService.getUserList(userList1);
            log.info("getUserList =====> " + JSON.toJSONString(userList2));

            Map<String, User> userMap1 = Map.of("7", new User(7, "name7"),
                    "8", new User(8, "name8"),
                    "9", new User(9, "name9")
            );
            Map<String, User> userMap2 = userService.getUserMap(userMap1);
            log.info("getUserMap =====> " + JSON.toJSONString(userMap2));

            Map<String, User> userMap3 = userService.userListToMap(userList1);
            log.info("userListToMap =====> " + JSON.toJSONString(userMap3));*/
        };
    }
}
