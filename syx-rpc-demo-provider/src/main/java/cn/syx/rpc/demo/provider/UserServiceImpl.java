package cn.syx.rpc.demo.provider;

import cn.syx.rpc.core.annotation.SyxProvider;
import cn.syx.rpc.demo.api.User;
import cn.syx.rpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@SyxProvider
public class UserServiceImpl implements UserService {

    @Autowired
    private Environment environment;

    @Override
    public User findById(int id) {
        return new User(id, "syx" + "-" + "V3" + "-" + environment.getProperty("server.port") + "-" + System.currentTimeMillis());
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

    @Override
    public List<User> getUserList(List<User> users) {
        users.add(new User(111, "getUserList-syx"));
        return users;
    }

    @Override
    public Map<String, User> getUserMap(Map<String, User> users) {
        users.put("getUserMap-syx", new User(222, "getUserMap-syx"));
        return users;
    }

    @Override
    public Map<String, User> userListToMap(List<User> users) {
        Map<String, User> userMap = users.stream().collect(Collectors.toMap(User::getName, user -> user));
        userMap.put("userListToMap-syx", new User(333, "userListToMap-syx"));
        return userMap;
    }
}
