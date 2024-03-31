package cn.syx.rpc.core.meta;

import com.alibaba.fastjson2.JSON;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class InstanceMeta {

    private String schema;
    private String host;
    private Integer port;
    private String context;

    private boolean status;// online or offline
    private Map<String, String> parameters = new HashMap<>();

    public String toPath() {
        return String.format("%s_%d", host, port);
    }

    public static InstanceMeta http(String path) {
        String[] parts = path.split("_");
        return http(parts[0], Integer.parseInt(parts[1]));
    }

    public static InstanceMeta http(String host, int port) {
        InstanceMeta instance = new InstanceMeta();
        instance.setSchema("http");
        instance.setHost(host);
        instance.setPort(port);
        instance.setContext("");
        return instance;
    }

    public String toUrl() {
        return String.format("%s://%s:%d/%s", schema, host, port, context);
    }

    public String toMetas() {
        return JSON.toJSONString(parameters);
    }
}
