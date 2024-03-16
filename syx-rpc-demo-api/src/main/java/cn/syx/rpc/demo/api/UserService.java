package cn.syx.rpc.demo.api;

import java.util.List;
import java.util.Map;

public interface UserService {

    User findById(int id);

    int parseUser(User user);

    Map<String, Object> findByIdWithMap(int id);

    User convertTOUser(Map<String, Object> map);

    List<User> getUserList(List<User> users);

    Map<String, User> getUserMap(Map<String, User> users);

    Map<String, User> userListToMap(List<User> users);
}
