package cn.syx.rpc.core.configcenter.syx;

import cn.syx.rpc.core.configcenter.DynamicRequestTime;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.event.EventListener;

import java.util.*;


@Data
@Slf4j
public class SyxTimeoutChangedListener implements DynamicRequestTime {

    private static Map<String, ConsumerTimeoutConfig> timeoutConfigMap = new HashMap<>();

    @Value("${syx.rpc.dynamic.timeout:}")
    private String timeoutJson;

    @Override
    public Integer getSocketTimeout(String serviceName, String methodName) {
        return doGetTimeout(serviceName, methodName);
    }

    @Override
    public Integer getConnectionTimeout(String serviceName, String methodName) {
        return null;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initConfig() {
        receiveData();
        log.debug("TimeoutChangedListener init config end");
    }

    @EventListener(EnvironmentChangeEvent.class)
    public void changeConfig(EnvironmentChangeEvent changeEvent) {
        Set<String> keys = changeEvent.getKeys();
        log.debug("TimeoutChangedListener change keys={}", keys);
        if (Objects.nonNull(keys) && !keys.contains("syx.rpc.dynamic.timeout")) {
            return;
        }
        receiveData();
        log.debug("TimeoutChangedListener change config end");
    }

    private void receiveData() {
        try {
            String content = "{}";
            if (Objects.nonNull(timeoutJson) && !Objects.equals("", timeoutJson)) {
                content = timeoutJson;
            }
            Map<String, ConsumerTimeoutConfig> temp = JSON.parseObject(content, new TypeReference<Map<String, ConsumerTimeoutConfig>>() {
            });
            timeoutConfigMap = temp;
        } catch (Exception e) {
            log.error("TimeoutChangedListener receiveData parse error", e);
        }
    }

    public static Integer doGetTimeout(String serviceName, String methodName) {
        ConsumerTimeoutConfig timeoutConfig = timeoutConfigMap.get(serviceName);
        if (Objects.isNull(timeoutConfig)) {
            return null;
        }

        Integer timeout = timeoutConfig.getMethodTimeoutMap().get(methodName);
        return Optional.of(timeout).orElse(timeoutConfig.getTimeout());
    }

    @Data
    public static class ConsumerTimeoutConfig {
        private Integer timeout;
        private Map<String, Integer> methodTimeoutMap = new HashMap<>();
    }
}
