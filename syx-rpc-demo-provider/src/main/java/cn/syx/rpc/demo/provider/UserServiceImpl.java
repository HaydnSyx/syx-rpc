package cn.syx.rpc.demo.provider;

import cn.syx.rpc.core.annotation.SyxProvider;
import cn.syx.rpc.demo.api.User;
import cn.syx.rpc.demo.api.UserService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
@SyxProvider
public class UserServiceImpl implements UserService {

    @Override
    public User findById(int id) {
        return new User(id, "syx" + System.currentTimeMillis());
    }
}
