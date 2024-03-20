package cn.syx.rpc.core.meta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务描述
 *
 * @author sunyongxue
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMeta {

    private String app;
    private String namespace;
    private String env;
    private String name;
    private String version = "1.0.0";

    public String toPath() {
        return String.format("%s_%s_%s_%s_%s", app, namespace, env, name, version);
    }
}
