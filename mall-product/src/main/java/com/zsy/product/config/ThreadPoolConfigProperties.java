package com.zsy.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author ZSY
 * TODO: 将这个类和配置文件中的某个前缀的配属属性进行绑定，这种方式也是可以的，但是也哭在配置类中直接使用@Value(value= "${}")
 */
@ConfigurationProperties(prefix = "gulimall.thread")
// @Component
@Data
public class ThreadPoolConfigProperties {

    private Integer coreSize;

    private Integer maxSize;

    private Integer keepAliveTime;

}
