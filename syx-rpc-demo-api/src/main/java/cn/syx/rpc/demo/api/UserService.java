package cn.syx.rpc.demo.api;

import java.util.Map;

public interface UserService {

    User findById(int id);

    int parseUser(User user);

    Map<String, Object> findByIdWithMap(int id);

    User convertTOUser(Map<String, Object> map);
}
