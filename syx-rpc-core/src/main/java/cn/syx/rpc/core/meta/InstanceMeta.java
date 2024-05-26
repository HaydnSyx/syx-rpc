package cn.syx.rpc.core.meta;

import cn.syx.registry.core.model.RegistryInstanceMeta;
import com.alibaba.fastjson2.JSON;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class InstanceMeta extends RegistryInstanceMeta {

    public String toPath() {
        return String.format("%s_%d", getHost(), getPort());
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
        instance.setPath("rpc");
        return instance;
    }

    public String toUrl() {
        return String.format("%s://%s:%d/%s", getSchema(), getHost(), getPort(), getPath());
    }

    public String toMetas() {
        return JSON.toJSONString(getParameters());
    }

    public InstanceMeta addParam(String key, String value) {
        getParameters().put(key, value);
        return this;
    }
}
