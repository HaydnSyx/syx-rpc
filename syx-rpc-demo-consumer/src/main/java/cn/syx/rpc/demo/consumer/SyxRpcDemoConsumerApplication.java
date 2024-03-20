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
    public User findById(@RequestParam int id) {
        return userService.findById(id);
    }

    @Bean
    @Order(Integer.MAX_VALUE)
    public ApplicationRunner consumer_runner() {
        return x -> {
            User user = userService.findById(100);
            System.out.println("getUser(100) =====> " + user);

            /*int num = demoService.aaa();
            System.out.println("aaa() =====> " + num);

            num = demoService.aaa(10);
            System.out.println("aaa(int) =====> " + num);

            num = demoService.aaa(Integer.valueOf(9999));
            System.out.println("aaa(Integer) =====> " + num);

            num = demoService.aaa(888, "syx");
            System.out.println("aaa(int, String) =====> " + num);

            User user1 = new User(999, "syx");
            int num4 = demoService.aaa(user1);
            System.out.println("aaa(user) =====> " + num4);

            long result = demoService.aaa(100L);
            System.out.println("aaa(long) =====> " + result);

            demoService.bbb("syx");

            cn.syx.rpc.demo.api.Order order = orderService.findById(1L);
            System.out.println("getOrder(1) =====> " + order);

//            order = orderService.findById(404L);
//            System.out.println("getOrder(404) =====> " + order);

            User user2 = userService.findById(100);
            System.out.println("findById(100) =====> " + user2);

            user2.setId(200);
            int id = userService.parseUser(user);
            System.out.println("parseUser(user) =====> " + id);


            Map<String, Object> map = userService.findByIdWithMap(300);
            System.out.println("findByIdWithMap(300) =====> " + JSON.toJSONString(map));
            User user3 = userService.convertTOUser(Map.of("id", 200, "name", "syx"));
            System.out.println("convertTOUser(map) =====> " + user3);

            int[] jjj = demoService.jjj(100);
            System.out.println("jjj(100) =====> " + JSON.toJSONString(jjj));

            long[] kkk = demoService.kkk();
            System.out.println("kkk() =====> " + JSON.toJSONString(kkk));

//            int[] mmm = demoService.mmm(new int[]{1, 1, 2, 2, 3, 3});
//            System.out.println("mmm([1,1,2,2,3,3]) =====> " + JSON.toJSONString(mmm));

            List<User> userList1 = Arrays.asList(
                    new User(1, "name1"),
                    new User(2, "name2"),
                    new User(3, "name3")
            );
            List<User> userList2 = userService.getUserList(userList1);
            System.out.println("getUserList =====> " + JSON.toJSONString(userList2));

            Map<String, User> userMap1 = Map.of("7", new User(7, "name7"),
                    "8", new User(8, "name8"),
                    "9", new User(9, "name9")
            );
            Map<String, User> userMap2 = userService.getUserMap(userMap1);
            System.out.println("getUserMap =====> " + JSON.toJSONString(userMap2));

            Map<String, User> userMap3 = userService.userListToMap(userList1);
            System.out.println("userListToMap =====> " + JSON.toJSONString(userMap3));*/
        };
    }
}
