package cn.syx.rpc.core.filter;

import cn.syx.rpc.core.annotation.SyxFilter;
import cn.syx.rpc.core.api.Filter;
import cn.syx.rpc.core.api.RpcRequest;
import cn.syx.rpc.core.api.RpcResponse;
import cn.syx.rpc.core.utils.MethodUtil;
import cn.syx.rpc.core.utils.MockUtil;

import java.lang.reflect.Method;
import java.util.Objects;

@SyxFilter(name = "mock", order = 0)
public class MockFilter implements Filter {

    @Override
    public Object preFilter(RpcRequest request) {
        try {
            Class<?> cls = Class.forName(request.getService());
            Method[] methods = cls.getMethods();
            // 获取目标方法
            Method method = MethodUtil.findMethodByMethodSign(methods, request.getMethodSign());
            if (Objects.isNull(method)) {
                return null;
            }
            // 获取参数返回类型
            Class<?> returnType = method.getReturnType();
            return MockUtil.mock(returnType);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Object postFilter(RpcRequest request, RpcResponse<?> response, Object result) {
        return null;
    }
}
