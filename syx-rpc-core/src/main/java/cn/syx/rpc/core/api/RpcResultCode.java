package cn.syx.rpc.core.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RpcResultCode {
    // 系统响应码
    SUCCESS("sys", "000", "响应成功"),
    FAIL("sys", "001", "响应失败"),
    TIMEOUT("sys", "002", "响应超时"),
    NO_SUCH_METHOD("sys", "003", "目标方法不存在"),
    EXECUTE_LIMIT("sys", "004", "执行次数限制"),
    UNKNOWN_ERROR("sys", "999", "未知错误"),

    PARAM_ERROR("biz", "001", "参数错误"),
    ;

    private final String type;
    private final String code;
    private final String msg;

    public String getResponseCode() {
        return String.format("%s-%s", this.type, this.code);
    }
}
