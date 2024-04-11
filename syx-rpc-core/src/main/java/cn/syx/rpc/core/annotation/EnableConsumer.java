package cn.syx.rpc.core.annotation;


import cn.syx.rpc.core.config.ConsumerConfig;
import cn.syx.rpc.core.config.ProviderConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Import(ConsumerConfig.class)
public @interface EnableConsumer {

}
