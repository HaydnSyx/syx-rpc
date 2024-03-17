package cn.syx.rpc.core.registry;

import cn.syx.rpc.core.api.RegistryCenter;
import com.alibaba.fastjson2.JSON;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;

public class ZkRegistryCenter implements RegistryCenter {

    private CuratorFramework client = null;

    @Override
    public void start() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString("localhost:2181")
                .namespace("SyxRpc")
                .retryPolicy(retryPolicy)
                .build();
        System.out.println("====> ZkRegistryCenter started");
        client.start();
    }

    @Override
    public void stop() {
        client.close();
    }

    @Override
    public void register(String service, String instance) {
        String servicePath = "/" + service;
        try {
            // 创建服务的持久节点
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
            }

            // 创建实例的临时节点
            String instancePath = servicePath + "/" + instance;
            System.out.println("====> tod register service: " + service + ", instance: " + instance);
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "provider".getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unregister(String service, String instance) {
        String servicePath = "/" + service;
        try {
            // 判断服务是否存在
            if (client.checkExists().forPath(servicePath) == null) {
                return;
            }

            // 删除实例的临时节点
            String instancePath = servicePath + "/" + instance;
            System.out.println("====> to unregister service: " + service + ", instance: " + instance);
            client.delete().quietly().forPath(instancePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> fetchAll(String serviceName) {
        String servicePath = "/" + serviceName;
        try {
            List<String> nodes = client.getChildren().forPath(servicePath);
            System.out.println("====> fetchAll service: " + serviceName + ", nodes: " + JSON.toJSON(nodes));
            return nodes;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void subscribe(String service, ChangeListener listener) {
        final TreeCache cache = TreeCache.newBuilder(client, "/" + service)
                .setCacheData(true)
                .setMaxDepth(2)
                .build();
        try {
            cache.start();
            cache.getListenable().addListener((cl, event) -> {
                System.out.println("====> subscribe event: " + event);
                List<String> nodes = fetchAll(service);
                listener.fire(new Event(nodes));
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
