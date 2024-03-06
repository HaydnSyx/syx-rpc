package cn.syx.rpc.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SyxRpcApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SyxRpcApiApplication.class, args);
    }
}
