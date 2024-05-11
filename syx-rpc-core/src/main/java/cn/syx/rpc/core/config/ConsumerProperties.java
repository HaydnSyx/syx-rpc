package cn.syx.rpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "syxrpc.consumer")
public class ConsumerProperties {

    private int retries = 1;

    private int timeout = 1000;

    private int connectionTimeout = 1000;

    private boolean enableFaultTolerance = false;

    private int faultLimit = 10;

    private int halfOpenInitialDelay = 10_000;

    private int halfOpenDelay = 60_000;

    private int grayRatio = 0;
}
