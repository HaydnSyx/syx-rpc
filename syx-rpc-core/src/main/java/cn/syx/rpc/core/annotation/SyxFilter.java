package cn.syx.rpc.core.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface SyxFilter {

    String name();

    boolean global() default true;

    int order() default 0;
}
