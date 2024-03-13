package cn.syx.rpc.demo.api;

public interface DemoService {

    int aaa();

    int aaa(int id);

    int aaa(int id, String name);

    int aaa(Integer id);

    int aaa(User user);

    void bbb(String name);

    long ccc(int id);

    double ddd(int id);

    float fff(int id);

    Integer eee(int id);

    boolean ggg(int id);

    String hhh(int id);

    Object iii(int id);
}
