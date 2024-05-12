package cn.syx.rpc.core.configcenter;

public interface DynamicRequestTime {

    Integer getSocketTimeout(String serviceName, String methodName);

    Integer getConnectionTimeout(String serviceName, String methodName);
}
