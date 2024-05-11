package cn.syx.rpc.core.api;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RpcConsumerContext {

    private int retries;
    private int timeout;
    private int connectionTimeout;
    private boolean enableFaultTolerance;
    private int faultLimit;
    private int halfOpenInitialDelay;
    private int halfOpenDelay;
    private int grayRatio;

    private List<String> filters = new ArrayList<>();

    // Map<方法名, 超时时间>
    private Map<String, Integer> timeoutMap = new HashMap<>();
    // Map<方法名, 重试次数>
    private Map<String, Integer> retrytMap = new HashMap<>();
    // Map<方法名, 过滤器列表>
    private Map<String, List<String>> filterMap = new HashMap<>();
}
