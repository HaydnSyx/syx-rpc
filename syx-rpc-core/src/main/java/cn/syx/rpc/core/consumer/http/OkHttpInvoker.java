package cn.syx.rpc.core.consumer.http;

import cn.syx.rpc.core.api.RpcConsumerContext;
import cn.syx.rpc.core.api.RpcException;
import cn.syx.rpc.core.api.RpcRequest;
import cn.syx.rpc.core.api.RpcResponse;
import cn.syx.rpc.core.consumer.HttpInvoker;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OkHttpInvoker implements HttpInvoker {

    private static final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client;

    public OkHttpInvoker(RpcConsumerContext consumerContext) {
        this.client = new OkHttpClient().newBuilder()
                .addInterceptor(new DynamicConnectTimeout(consumerContext))
                .connectionPool(new ConnectionPool(16, 1, TimeUnit.MINUTES))
                .readTimeout(consumerContext.getTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(consumerContext.getTimeout(), TimeUnit.MILLISECONDS)
                .connectTimeout(consumerContext.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public RpcResponse<?> post(RpcRequest request, String url) {
        String body = JSON.toJSONString(request);

        Request req = new Request.Builder()
                .url(url)
                .post(RequestBody.create(body, mediaType))
                .build();
        try (Response response = client.newCall(req).execute()) {
            ResponseBody responseBody = response.body();
            if (Objects.isNull(responseBody)) {
                throw new RuntimeException("provider response is null");
            }

            String data = responseBody.string();
            log.debug("provider response ======> {}", data);
            return JSON.parseObject(data, RpcResponse.class);
        } catch (Exception e) {
            return new RpcResponse<>(false, null, new RpcException(e.getMessage()));
        }
    }

    @Override
    public String post(String requestStr, String url) {
        return null;
    }

    @Override
    public String get(String url) {
        return null;
    }
}
