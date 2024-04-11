package cn.syx.rpc.core.api;

import lombok.Getter;

@Getter
public class RpcException extends RuntimeException {

    // B =>  业务类异常
    // S =>  系统类异常
    // U =>  未知异常
    public static final String SOCKET_TIME_EXCEPTION = "S001" + "-" + "HTTP-INVOKE" + "-" + "TIMEOUT";

    private String errCode;
    private String errMessage;

    public RpcException(RpcResultCode code) {
        super(code.getMsg());
        this.errCode = code.getResponseCode();
        this.errMessage = code.getMsg();
    }

    public RpcException(RpcResultCode code, String errMessage) {
        super(errMessage);
        this.errCode = code.getResponseCode();
        this.errMessage = errMessage;
    }

    public RpcException(String errCode, String errMessage) {
        super(errMessage);
        this.errCode = errCode;
        this.errMessage = errMessage;
    }

    public RpcException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public RpcException(String errMessage, Throwable cause) {
        super(errMessage, cause);
        this.errMessage = errMessage;
    }

    public RpcException(String errCode, String errMessage, Throwable cause) {
        super(errMessage, cause);
        this.errCode = errCode;
        this.errMessage = errMessage;
    }

    public RpcException(Throwable cause) {
        super(cause);
    }
}
