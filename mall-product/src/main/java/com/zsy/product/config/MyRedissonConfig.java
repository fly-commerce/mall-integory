package com.zsy.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author: zhangshuaiyin
 * @create: 2020-06-11 09:39
 **/

@Configuration
@RefreshScope
public class MyRedissonConfig {
    @Value(value = "${spring.redis.host}")
    private String redisHost;
    @Value(value = "${spring.redis.port}")
    private int port;
    @Value(value = "${spring.redis.database}")
    private int database;
    /**
     * 所有对 Redisson 的使用都是通过 RedissonClient
     *
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson() throws IOException {

        // 1、创建配置
        Config config = new Config();
        // Redis url should start with redis:// or rediss://
        // "redis://192.168.1.214:6379"
        // 这里是单节点模式登陆，如果要搭建redis集群，这种配置方式是不争取的。
        config.useSingleServer().setAddress("redis://" + redisHost + ":" + port).setDatabase(database);
        // 2、根据 Config 创建出 RedissonClient 实例
        return Redisson.create(config);
    }

}
