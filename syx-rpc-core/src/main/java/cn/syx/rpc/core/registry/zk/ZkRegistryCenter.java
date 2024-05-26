package cn.syx.rpc.core.registry.zk;

import cn.syx.registry.core.model.instance.RpcServiceMeta;
import cn.syx.rpc.core.api.RegistryCenter;
import cn.syx.rpc.core.meta.InstanceMeta;
import cn.syx.rpc.core.registry.RegistryChangeListener;
import cn.syx.rpc.core.registry.RegistryChangeEvent;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ZkRegistryCenter implements RegistryCenter {

    private CuratorFramework client = null;

    @Value("${syxrpc.zk.services}")
    private String zkServices;

    @Value("${syxrpc.zk.root}")
    private String zkRoot;

    @Override
    public void start() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString(zkServices)
//                .connectString("syx.workspace.com:2181,syx.workspace.com:2182,syx.workspace.com:2183")
                .namespace(zkRoot)
                .retryPolicy(retryPolicy)
                .build();
        log.info("====> ZkRegistryCenter started, services: " + zkServices + ", root: " + zkRoot);
        client.start();
    }

    @Override
    public void stop() {
        log.info("====> ZkRegistryCenter closed");
        client.close();
    }

    @Override
    public void register(RpcServiceMeta service, InstanceMeta instance) {
        String servicePath = "/" + service.identity();
        try {
            // 创建服务的持久节点
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, toMetas(service).getBytes());
            }

            // 创建实例的临时节点
            String instancePath = servicePath + "/" + instance.toPath();
            log.info("====> to register service: " + service + ", instance: " + instance);
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, instance.toMetas().getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String toMetas(RpcServiceMeta instanceMeta) {
        return String.format("%s_%s_%s_%s_%s",
                instanceMeta.getNamespace(),
                instanceMeta.getEnv(),
                instanceMeta.getGroup(),
                instanceMeta.getName(),
                instanceMeta.getVersion()
        );
    }

    @Override
    public void unregister(RpcServiceMeta service, InstanceMeta instance) {
        String servicePath = "/" + service.identity();
        try {
            // 判断服务是否存在
            if (client.checkExists().forPath(servicePath) == null) {
                return;
            }

            // 删除实例的临时节点
            String instancePath = servicePath + "/" + instance.toPath();
            log.info("====> to unregister service: " + service + ", instance: " + instance);
            client.delete().quietly().forPath(instancePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<InstanceMeta> fetchAll(RpcServiceMeta serviceName) {
        String servicePath = "/" + serviceName.identity();
        try {
            List<String> nodes = client.getChildren().forPath(servicePath);
//            log.info("====> fetchAll service: " + serviceName + ", nodes: " + JSON.toJSON(nodes));
            return nodes.stream().map(node -> {
                String nodePath = servicePath + "/" + node;
                byte[] data = null;
                try {
                    data = client.getData().forPath(nodePath);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                InstanceMeta instance = InstanceMeta.http(node);
                HashMap metas = JSON.parseObject(new String(data), HashMap.class);
                log.debug("====> fetchAll service: " + serviceName + ", metas: " + metas);
                instance.setParameters(metas);
                return instance;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void subscribe(RpcServiceMeta service, RegistryChangeListener listener) {
        final TreeCache cache = TreeCache.newBuilder(client, "/" + service.identity())
                .setCacheData(true)
                .setMaxDepth(2)
                .build();
        try {
            cache.start();
            cache.getListenable().addListener((cl, event) -> {
//                log.info("====> subscribe event: " + event);
                List<InstanceMeta> nodes = fetchAll(service);
                listener.fire(new RegistryChangeEvent(nodes));
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
