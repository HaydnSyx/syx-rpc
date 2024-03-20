package cn.syx.rpc.core.api;

import cn.syx.rpc.core.meta.InstanceMeta;
import cn.syx.rpc.core.meta.ServiceMeta;
import cn.syx.rpc.core.registry.ChangeListener;

import java.util.ArrayList;
import java.util.List;

public interface RegistryCenter {

    void start();

    void stop();

    void register(ServiceMeta service, InstanceMeta instance);

    void unregister(ServiceMeta service, InstanceMeta instance);

    List<InstanceMeta> fetchAll(ServiceMeta service);

    void subscribe(ServiceMeta service, ChangeListener listener);

    class StaticRegistryCenter implements RegistryCenter {

        private final List<String> providers;

        public StaticRegistryCenter(List<String> providers) {
            this.providers = providers;
        }

        @Override
        public void start() {
            // do nothing
        }

        @Override
        public void stop() {
            // do nothing
        }

        @Override
        public void register(ServiceMeta service, InstanceMeta instance) {
            // do nothing
        }

        @Override
        public void unregister(ServiceMeta service, InstanceMeta instance) {
            // do nothing
        }

        @Override
        public List<InstanceMeta> fetchAll(ServiceMeta service) {
            return new ArrayList<>();
        }

        @Override
        public void subscribe(ServiceMeta service, ChangeListener listener) {
            // do nothing
        }
    }
}
