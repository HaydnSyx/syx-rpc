package cn.syx.rpc.core.api;

import lombok.Data;

@Data
public class RpcRequest {

    /** 接口 */
    private String service;
    /** 方法 */
    private String method;
    private Class<?>[] parameterTypes;
    /** 参数数组 */
    private Object[] args;
}
