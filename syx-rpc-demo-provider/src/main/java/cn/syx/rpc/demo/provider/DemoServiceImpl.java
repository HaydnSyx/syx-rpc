package cn.syx.rpc.demo.provider;

import cn.syx.rpc.core.annotation.SyxProvider;
import cn.syx.rpc.demo.api.DemoService;
import cn.syx.rpc.demo.api.User;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
@SyxProvider
public class DemoServiceImpl implements DemoService {

    @Override
    public int aaa() {
        return 666;
    }

    @Override
    public int aaa(int id) {
        System.out.println("进入aaa(int id)方法中: param=" + id);
        return 777;
    }

    @Override
    public int aaa(Integer id) {
        System.out.println("进入aaa(Integer id)方法中: param=" + id);
        return 888;
    }

    @Override
    public int aaa(User user) {
        System.out.println("进入aaa(User user)方法中: param=" + user);
        return 999;
    }

    @Override
    public void bbb(String name) {
        System.out.println("bbb: " + name);
    }

    @Override
    public long ccc(int id) {
        return 123L;
    }

    @Override
    public double ddd(int id) {
        return 1.23d;
    }

    @Override
    public float fff(int id) {
        return 5.67f;
    }

    @Override
    public Integer eee(int id) {
        if (id == 1) {
            return 1;
        }
        return null;
    }

    @Override
    public boolean ggg(int id) {
        if (id == 2) {
            return true;
        }
        return false;
    }

    @Override
    public String hhh(int id) {
        if (id == 10) {
            return null;
        }
        return "abc";
    }

    @Override
    public Object iii(int id) {
        if (id == 10) {
            return null;
        }
        return 1232;
    }
}
