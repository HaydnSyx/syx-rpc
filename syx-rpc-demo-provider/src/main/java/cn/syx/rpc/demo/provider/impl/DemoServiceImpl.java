package cn.syx.rpc.demo.provider.impl;

import cn.syx.rpc.core.annotation.SyxProvider;
import cn.syx.rpc.demo.api.DemoService;
import cn.syx.rpc.demo.api.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@SyxProvider
public class DemoServiceImpl implements DemoService {

    @Autowired
    private Environment environment;

    @Override
    public int aaa() {
        return 666;
    }

    @Override
    public int aaa(int id) {
        log.debug("进入aaa(int id)方法中: param=" + id);
        return 777;
    }

    @Override
    public int aaa(int id, String name) {
        log.debug("进入aaa(int id String name)方法中: param=" + id + ", " + name);
        return 789;
    }

    @Override
    public int aaa(Integer id) {
        log.debug("进入aaa(Integer id)方法中: param=" + id);
        return 888;
    }

    @Override
    public int aaa(User user) {
        log.debug("进入aaa(User user)方法中: param=" + user);
        return 999;
    }

    @Override
    public long aaa(long xxx) {
        return 10000L + xxx;
    }

    @Override
    public void bbb(String name) {
        log.debug("bbb: " + name);
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

    @Override
    public int[] jjj(int id) {
        log.debug("jjj: " + id);
        return new int[]{1, 2, 3, 4, 5};
    }

    @Override
    public long[] kkk() {
        return new long[]{1L, 2L, 3L};
    }

    @Override
    public int[] mmm(int[] a) {
        return a;
    }

    @Override
    public User findWithTimeout(int id, int timeout, boolean fireTimeout) {
        String port = environment.getProperty("server.port");
        if ("6081".equals(port) && fireTimeout) {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return new User(id, "syx-timeout-" + "-" + environment.getProperty("server.port") + "-" + System.currentTimeMillis());
    }
}
