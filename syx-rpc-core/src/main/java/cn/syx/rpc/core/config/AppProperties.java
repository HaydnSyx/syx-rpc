package cn.syx.rpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "syxrpc.app")
public class AppProperties {

    // for app instance
    private String id = "app1";

    private String namespace = "default";

    private String env = "dev";

    private String version = "1.0.0";
}
