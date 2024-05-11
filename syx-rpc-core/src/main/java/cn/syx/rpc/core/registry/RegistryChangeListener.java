package cn.syx.rpc.core.registry;

public interface RegistryChangeListener {
    void fire(RegistryChangeEvent event);
}
