package cn.syx.rpc.core.api;

import lombok.Data;

@Data
public class RpcRequest {

    /** 接口 */
    private String service;
    /** 参数数组 */
    private Object[] args;
    /** 方法签名 */
    private String methodSign;
}
