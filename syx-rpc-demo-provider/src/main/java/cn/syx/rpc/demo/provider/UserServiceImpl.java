package cn.syx.rpc.demo.provider;

import cn.syx.rpc.core.annotation.SyxProvider;
import cn.syx.rpc.demo.api.User;
import cn.syx.rpc.demo.api.UserService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@SyxProvider
public class UserServiceImpl implements UserService {

    @Override
    public User findById(int id) {
        return new User(id, "syx" + System.currentTimeMillis());
    }

    @Override
    public int parseUser(User user) {
        return user.getId();
    }

    @Override
    public Map<String, Object> findByIdWithMap(int id) {
        User user = new User(id, "syx" + System.currentTimeMillis());
        return Map.of("id", user.getId(), "name", user.getName());
    }

    @Override
    public User convertTOUser(Map<String, Object> map) {
        Object o = map.get("id");
        return new User(Integer.parseInt(o.toString()), String.valueOf(map.get("name")));
    }
}
