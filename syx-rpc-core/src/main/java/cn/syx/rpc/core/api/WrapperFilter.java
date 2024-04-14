package cn.syx.rpc.core.api;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class WrapperFilter {

    private int order;

    private boolean global;

    private Filter filter;
}
