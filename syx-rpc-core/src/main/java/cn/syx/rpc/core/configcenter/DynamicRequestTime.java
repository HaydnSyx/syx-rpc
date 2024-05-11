package cn.syx.rpc.core.configcenter;

public interface DynamicRequestTime {

    Integer getTimeout(String serviceName, String methodName);
}
