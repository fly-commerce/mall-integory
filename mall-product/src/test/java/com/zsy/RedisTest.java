package com.zsy;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;
/**
 * @Description redis测试类
 * @Author fly-ftx
 * @Date 21:05 2021/5/9
 * @Param
 * @return
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
class RedisTest {

//    @Autowired
//    private StringRedisTemplate stringRedisTemplate;
//
//
//    @Test
//    void contextLoads() {
//    }
//
//    @Test
//    void testStringRedisTemplate() {
//        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
//        System.out.println("redis链接信息：" + stringRedisTemplate);
//        // 保存
//        ops.set("hello" , "world_ligel" + UUID.randomUUID());
//        // 查询
//        String hello = ops.get("hello");
//        System.out.println("获取的结果：" + hello);
//    }

}
