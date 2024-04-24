package cn.syx.rpc.core.consumer;

import cn.syx.rpc.core.api.RpcRequest;
import cn.syx.rpc.core.api.RpcResponse;
import cn.syx.rpc.core.consumer.http.DynamicConnectTimeout;
import cn.syx.rpc.core.consumer.http.OkHttpInvoker;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public interface HttpInvoker {

    Logger log = LoggerFactory.getLogger(HttpInvoker.class);

    HttpInvoker DEFAULT = new HttpInvoker() {

        private static final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

        private final OkHttpClient client = new OkHttpClient().newBuilder()
                .connectionPool(new ConnectionPool(16, 1, TimeUnit.MINUTES))
                .readTimeout(500, TimeUnit.MILLISECONDS)
                .writeTimeout(500, TimeUnit.MILLISECONDS)
                .connectTimeout(500, TimeUnit.MILLISECONDS)
                .build();

        @Override
        public RpcResponse<?> post(RpcRequest request, String url) {
            return null;
        }

        @Override
        public String post(String requestStr, String url) {
            Request req = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(requestStr, mediaType))
                    .build();
            try (Response response = client.newCall(req).execute()) {
                ResponseBody responseBody = response.body();
                if (Objects.isNull(responseBody)) {
                    throw new RuntimeException("provider response is null");
                }

                String data = responseBody.string();
                log.debug("provider response ======> {}", data);
                return data;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String get(String url) {
            Request req = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            try (Response response = client.newCall(req).execute()) {
                ResponseBody responseBody = response.body();
                if (Objects.isNull(responseBody)) {
                    throw new RuntimeException("provider response is null");
                }

                String data = responseBody.string();
                log.debug("provider response ======> {}", data);
                return data;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    };

    RpcResponse<?> post(RpcRequest request, String url);

    String post(String requestStr, String url);

    String get(String url);

    static <T> T httpGet(String url, Class<T> clazz) {
        log.debug(" =====>>>>>> httpGet: " + url);
        String data = DEFAULT.get(url);
        log.debug(" =====>>>>>> response: " + data);
        return JSON.parseObject(data, clazz);
    }

    static <T> T httpGet(String url, TypeReference<T> ref) {
        log.debug(" =====>>>>>> httpGet: " + url);
        String data = DEFAULT.get(url);
        log.debug(" =====>>>>>> response: " + data);
        return JSON.parseObject(data, ref);
    }


    static <T> T httpPost(String requestStr, String url, Class<T> clazz) {
        log.debug(" =====>>>>>> httpPost: " + url);
        String data = DEFAULT.post(requestStr, url);
        log.debug(" =====>>>>>> response: " + data);
        return JSON.parseObject(data, clazz);
    }

    static <T> T httpPost(String requestStr, String url, TypeReference<T> ref) {
        log.debug(" =====>>>>>> httpPost: " + url);
        String data = DEFAULT.post(requestStr, url);
        log.debug(" =====>>>>>> response: " + data);
        return JSON.parseObject(data, ref);
    }
}
