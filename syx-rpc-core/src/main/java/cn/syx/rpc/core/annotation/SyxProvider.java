package cn.syx.rpc.core.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface SyxProvider {

    String namespace() default "";

    String group() default "";

    String version() default "";
}
