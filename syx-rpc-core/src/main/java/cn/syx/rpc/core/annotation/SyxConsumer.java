package cn.syx.rpc.core.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface SyxConsumer {

    String namespace() default "";

    String group() default "";

    String version() default "";

    int connectionTimeout() default -1;

    int timeout() default -1;

    int retries() default -1;

    String[] filters() default {};

    MethodCustomer[] methodCustomers() default {};

    @interface MethodCustomer {
        String methodName();

        int retries() default -1;

        int timeout() default -1;

        String[] filters() default {};
    }
}
