package cn.syx.rpc.demo.provider.impl;

import cn.syx.rpc.core.annotation.SyxProvider;
import cn.syx.rpc.demo.api.Order;
import cn.syx.rpc.demo.api.OrderService;
import org.springframework.stereotype.Service;

@Service
@SyxProvider
public class OrderServiceImpl implements OrderService {

    @Override
    public Order findById(long id) {
        if (id == 404L) {
            throw new RuntimeException("测试错误");
        }
        return new Order(id, 12.3d);
    }
}
