package cn.syx.rpc.core.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse<T> {

    private boolean status;

    private T data;

    private RpcException ex;
}
