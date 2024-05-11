package cn.syx.rpc.core.registry;

import cn.syx.rpc.core.meta.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistryChangeEvent {

    private List<InstanceMeta> data;
}
