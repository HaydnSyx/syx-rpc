package cn.syx.rpc.core.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.internals.DefaultConfig;
import com.ctrip.framework.apollo.internals.SimpleConfig;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


@Data
@Slf4j
public class TimeoutChangedListener {

    private static Map<String, ConsumerTimeoutConfig> timeoutConfigMap = new HashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void initConfig() {
        try {
            Config config = ConfigService.getConfig("syx-rpc-consumer-timeout.json");
            String content = config.getProperty("content", "{}");
            log.debug("TimeoutChangedListener initConfig, content: {}", content);
            Map<String, ConsumerTimeoutConfig> temp = JSON.parseObject(content, new TypeReference<Map<String, ConsumerTimeoutConfig>>() {
            });
            timeoutConfigMap = temp;
        } catch (Exception e) {
            log.error("TimeoutChangedListener initConfig error", e);
        }
    }

    @ApolloConfigChangeListener({"syx-app", "syx-rpc-consumer-timeout.json"})
    public void timeChangeHandler(ConfigChangeEvent changeEvent) {
        for (String key : changeEvent.changedKeys()) {
            ConfigChange change = changeEvent.getChange(key);
            try {
                String newValue = change.getNewValue();
                log.debug("TimeoutChangedListener changeHandler, key: {}, change: {}", key, newValue);
                Map<String, ConsumerTimeoutConfig> temp = JSON.parseObject(newValue, new TypeReference<Map<String, ConsumerTimeoutConfig>>() {
                });
                timeoutConfigMap = temp;
            } catch (Exception e) {
                log.error("TimeoutChangedListener changeHandler error, key: {}, change: {}", key, change, e);
            }
        }
    }

    public static Integer getTimeout(String serviceName, String methodName) {
        ConsumerTimeoutConfig timeoutConfig = timeoutConfigMap.get(serviceName);
        if (Objects.isNull(timeoutConfig)) {
            return null;
        }

        return timeoutConfig.getMethodTimeoutMap().get(methodName);
    }

    @Data
    public static class ConsumerTimeoutConfig {
        private Integer timeout;
        private Map<String, Integer> methodTimeoutMap = new HashMap<>();
    }
}
