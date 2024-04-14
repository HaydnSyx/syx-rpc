package cn.syx.rpc.demo.consumer;

import cn.syx.rpc.core.annotation.EnableConsumer;
import cn.syx.rpc.core.annotation.EnableProvider;
import cn.syx.rpc.core.annotation.SyxConsumer;
import cn.syx.rpc.demo.api.DemoService;
import cn.syx.rpc.demo.api.User;
import cn.syx.rpc.demo.api.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Slf4j
@RestController
@SpringBootApplication
@EnableConsumer
public class SyxRpcDemoConsumerApplication {

    @SyxConsumer
    private UserService userService;
    @SyxConsumer(timeout = 500,
            filters = {"customerFilter1", "customerFilter2", "customerFilter3"},
            methodCustomers = {
            @SyxConsumer.MethodCustomer(methodName = "bbb", timeout = 100, filters = {"customerFilter1"}),
//            @SyxConsumer.MethodCustomer(methodName = "ccc", timeout = 200),
            @SyxConsumer.MethodCustomer(methodName = "hhh", timeout = 300, filters = {"customerFilter1", "customerFilter2"})
    })
    private DemoService demoService;

    public static void main(String[] args) {
        SpringApplication.run(SyxRpcDemoConsumerApplication.class, args);
    }

    @GetMapping("/find/id")
    public User findById(@RequestParam("id") int id) {
        return userService.findById(id);
    }

    @GetMapping("/find/timeout")
    public User findWithTimeout(@RequestParam("id") int id,
                                @RequestParam("timeout") int timeout,
                                @RequestParam(value = "fireTimeout", defaultValue = "true") boolean fireTimeout) {
        return demoService.findWithTimeout(id, timeout, fireTimeout);
    }

    @GetMapping("/demo")
    public Object findById(@RequestParam("method") String method, @RequestParam("param") String param) {
        if (Objects.equals("bbb", method)) {
            demoService.bbb(param);
            return "success";
        }
        if (Objects.equals("ccc", method)) {
            return demoService.ccc(Integer.parseInt(param));
        }
        if (Objects.equals("hhh", method)) {
            return demoService.hhh(Integer.parseInt(param));
        }
        return "not support method";
    }
}
