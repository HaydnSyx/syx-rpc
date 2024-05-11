package cn.syx.rpc.core.transport;

import cn.syx.rpc.core.api.RpcException;
import cn.syx.rpc.core.api.RpcResponse;
import cn.syx.rpc.core.api.Transporter;
import cn.syx.rpc.core.configcenter.DynamicRequestTime;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CommonHttpTransporter implements Transporter {

    private static final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client;

    public CommonHttpTransporter() {
        this(128, 60, 1000, 1000, new DynamicConnectTimeout(null));
    }

    public CommonHttpTransporter(DynamicRequestTime requestTime) {
        this(128, 60, 1000, 1000, new DynamicConnectTimeout(requestTime));
    }

    public CommonHttpTransporter(int maxConnections, int keepLiveSec, int connectionTimeout, int scopeTimeout, Interceptor interceptor) {
        this.client = new OkHttpClient().newBuilder()
                .addInterceptor(interceptor)
                .connectionPool(new ConnectionPool(maxConnections, keepLiveSec, TimeUnit.SECONDS))
                .readTimeout(scopeTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(scopeTimeout, TimeUnit.MILLISECONDS)
                .connectTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public RpcResponse<?> invoke(byte[] request, String url) {
        Request req = new Request.Builder()
                .url(url)
                .post(RequestBody.create(request, mediaType))
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
}
