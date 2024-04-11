package cn.syx.rpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "syxrpc.provider")
public class ProviderProperties {

    private int port = -1;

    private Map<String, String> metas = new HashMap<>();
}
