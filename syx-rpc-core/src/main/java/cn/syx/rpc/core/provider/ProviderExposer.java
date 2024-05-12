package cn.syx.rpc.core.provider;

import cn.syx.rpc.core.api.RpcRequest;
import cn.syx.rpc.core.api.RpcResponse;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@ConditionalOnClass(value = TomcatServletWebServerFactory.class)
public class ProviderExposer {

    @Value("${syxrpc.provider.port:6070}")
    private String rpcPorts;

    @Autowired
    private ProviderInvoker invoker;

    @RequestMapping("/rpc")
    public RpcResponse<?> doInvoker(@RequestBody RpcRequest request) {
        return invoker.invokerRequest(request);
    }

    @Bean
    public TomcatServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        Connector[] additionalConnectors = this.additionalConnector();
        if (additionalConnectors.length > 0) {
            tomcat.addAdditionalTomcatConnectors(additionalConnectors);
        }
        return tomcat;
    }

    private Connector[] additionalConnector() {
        String[] ports = this.rpcPorts.split(",");
        List<Connector> result = new ArrayList<>();
        for (String port : ports) {
            Connector connector = new Connector("org.apache.coyote.http11.Http11Nio2Protocol");
            connector.setScheme("http");
            connector.setPort(Integer.parseInt(port));
            result.add(connector);
        }
        return result.toArray(new Connector[]{});
    }
}
