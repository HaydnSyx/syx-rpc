package cn.syx.rpc.core.api;

import java.util.List;

public interface RegistryCenter {

    void start();

    void stop();

    void register(String service, String instance);

    void unregister(String service, String instance);

    List<String> fetchAll(String serviceName);

//    void subscribe(String service, NotifyListener listener);

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
        public void register(String service, String instance) {
            // do nothing
        }

        @Override
        public void unregister(String service, String instance) {
            // do nothing
        }

        @Override
        public List<String> fetchAll(String serviceName) {
            return this.providers;
        }
    }
}
