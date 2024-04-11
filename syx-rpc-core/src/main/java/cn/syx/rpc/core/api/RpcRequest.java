package cn.syx.rpc.core.api;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class RpcRequest {

    /** 接口 */
    private String service;
    /** 参数数组 */
    private Object[] args;
    /** 方法签名 */
    private String methodSign;
    /** 跨调用方需要传递的参数 */
    private Map<String, String> params = new HashMap<>();
}
