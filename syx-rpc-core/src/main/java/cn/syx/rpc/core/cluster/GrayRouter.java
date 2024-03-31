package cn.syx.rpc.core.cluster;

import cn.syx.rpc.core.api.Router;
import cn.syx.rpc.core.meta.InstanceMeta;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class GrayRouter implements Router<InstanceMeta> {

    @Setter
    private int grayRatio;
    private final Random random = new Random();

    public GrayRouter(int grayRatio) {
        this.grayRatio = grayRatio;
    }

    @Override
    public List<InstanceMeta> route(List<InstanceMeta> providers) {

        if (Objects.isNull(providers) || providers.size() < 2) {
            return providers;
        }

        List<InstanceMeta> normalNodes = new ArrayList<>();
        List<InstanceMeta> grayNodes = new ArrayList<>();

        providers.forEach(e -> {
            String grayValue = e.getParameters().get("gray");
            if (Objects.equals(grayValue, "true")) {
                grayNodes.add(e);
            } else {
                normalNodes.add(e);
            }
        });

        if (normalNodes.isEmpty() || grayNodes.isEmpty()) {
            return providers;
        }

        if (grayRatio <= 0) {
            return normalNodes;
        } else if (grayRatio >= 100) {
            return grayNodes;
        } else {
            int randomValue = random.nextInt(100);
            return randomValue < grayRatio ? grayNodes : normalNodes;
        }
    }
}
