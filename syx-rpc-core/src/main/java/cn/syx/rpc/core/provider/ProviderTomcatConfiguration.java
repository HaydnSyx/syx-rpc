package cn.syx.rpc.core.provider;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

//@Slf4j
//@ConditionalOnClass(value = TomcatServletWebServerFactory.class)
public class ProviderTomcatConfiguration {

    @Value("${syxrpc.provider.port:8080}")
    private String rpcPorts;

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
