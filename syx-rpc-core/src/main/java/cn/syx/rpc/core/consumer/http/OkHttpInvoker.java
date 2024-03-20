package cn.syx.rpc.core.consumer.http;

import cn.syx.rpc.core.api.RpcRequest;
import cn.syx.rpc.core.api.RpcResponse;
import cn.syx.rpc.core.consumer.HttpInvoker;
import com.alibaba.fastjson2.JSON;
import okhttp3.*;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class OkHttpInvoker implements HttpInvoker {

    private static final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client;

    public OkHttpInvoker() {
        this.client = new OkHttpClient().newBuilder()
                .connectionPool(new ConnectionPool(16, 1, TimeUnit.MINUTES))
                .readTimeout(1, TimeUnit.SECONDS)
                .writeTimeout(1, TimeUnit.SECONDS)
                .connectTimeout(1, TimeUnit.SECONDS)
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
            System.out.println("provider response ======> " + data);
            return JSON.parseObject(data, RpcResponse.class);
        } catch (Exception e) {
            return new RpcResponse<>(false, null, e);
        }
    }
}
