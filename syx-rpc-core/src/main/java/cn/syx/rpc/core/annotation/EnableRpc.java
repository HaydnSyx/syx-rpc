package cn.syx.rpc.core.annotation;


import cn.syx.rpc.core.provider.ProviderConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
@Import(ProviderConfig.class)
public @interface EnableRpc {

}
