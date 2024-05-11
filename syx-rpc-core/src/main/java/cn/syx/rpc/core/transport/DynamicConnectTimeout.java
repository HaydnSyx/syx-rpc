package cn.syx.rpc.core.transport;

import cn.syx.rpc.core.api.RpcConsumerContext;
import cn.syx.rpc.core.api.RpcRequest;
import cn.syx.rpc.core.configcenter.DynamicRequestTime;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static cn.syx.rpc.core.consumer.ConsumerBootstrap.CONSUMER_CONTEXT_MAP;

@Slf4j
public class DynamicConnectTimeout implements Interceptor {

    private final DynamicRequestTime dynamicRequestTime;

    public DynamicConnectTimeout(DynamicRequestTime dynamicRequestTime) {
        this.dynamicRequestTime = dynamicRequestTime;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();

        RequestBody requestBody = request.body();
        try (okio.Buffer buffer = new okio.Buffer()) {
            requestBody.writeTo(buffer);
            String requestStr = buffer.readString(StandardCharsets.UTF_8);
            RpcRequest rpcRequest = JSON.parseObject(requestStr, RpcRequest.class);

            log.debug("===> invoker provider from body: {}", requestStr);

            // 获取方法签名
            String service = rpcRequest.getService();
            String methodSign = rpcRequest.getMethodSign();
            String methodName = methodSign.substring(0, methodSign.indexOf("("));

            RpcConsumerContext consumerContext = CONSUMER_CONTEXT_MAP.get(service);

            // 1.从配置中心中获取超时时间
            Integer timeout = null;
            if (Objects.nonNull(dynamicRequestTime)) {
                timeout = dynamicRequestTime.getTimeout(service, methodName);
            }

            // 2.启动配置中获取超时时间
            if (Objects.isNull(timeout)) {
                timeout = consumerContext.getTimeoutMap().get(methodName);
            }
            int socketTimeout = Optional.ofNullable(timeout).orElse(consumerContext.getTimeout());

            log.debug("===> {} invoker provider, connection_time={}ms, read_time={}ms, write_time={}ms",
                    methodSign, consumerContext.getConnectionTimeout(), socketTimeout, socketTimeout);

            return chain.withConnectTimeout(consumerContext.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                    .withReadTimeout(socketTimeout, TimeUnit.MILLISECONDS)
                    .withWriteTimeout(socketTimeout, TimeUnit.MILLISECONDS)
                    .proceed(request);
        } catch (Exception e) {
            log.error("===> invoker provider error", e);
        }

        return chain.proceed(request);
    }
}
