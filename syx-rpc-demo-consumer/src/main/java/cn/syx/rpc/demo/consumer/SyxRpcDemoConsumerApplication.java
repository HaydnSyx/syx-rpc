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

@Slf4j
@RestController
@SpringBootApplication
@EnableConsumer
public class SyxRpcDemoConsumerApplication {

    @SyxConsumer
    private UserService userService;
    @SyxConsumer
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
}
