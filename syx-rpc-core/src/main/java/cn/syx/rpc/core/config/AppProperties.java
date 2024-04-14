package cn.syx.rpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "syxrpc.app")
public class AppProperties {

    private String namespace = "default";

    private String group = "app";

    private String version = "1.0.0";

    private String env = "dev";
}
