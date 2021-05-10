package com.zsy.product.api;

import com.zsy.product.config.ElasticsearchConfig;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;



/**
 * TODO
 *
 * @author fly-ftx
 * @version 1.0
 * @date 2021/5/8 17:43
 */
@RestController
public class AtoolConfigController {

    /**
     * @Description 这里有个问题，elaticsearch进行了配置，这里不用@Qulifier(value= "esRestClient"),不然会匹配到两个bean
     *
     * @Author fly-ftx
     * @Date 20:28 2021/5/9
     * @Param
     * @return
     **/
    @Resource
    @Qualifier(value = "esRestClient")
    private RestHighLevelClient esClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redisson;
    /*TODO~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~elasticsearch相关测试~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

    /**
     * @Description 查询数据-从elasticsearch
     *               其实这里的操作还是很简单的，之类和kibana是一样的操作，构造相应的条件就可以了
     * @Author fly-ftx
     * @Date 16:01 2021/5/8
     * @Param []
     * @return void
     **/
    @GetMapping("/es/searchData")
    public String searchData() throws IOException {
        // 1. 创建检索请求
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("bank");
        // 指定检索条件，这里的检索条件和在kibana很相似
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 1.1)构造检索条件
        /*searchSourceBuilder.query();
        searchSourceBuilder.from();
        searchSourceBuilder.size();
        searchSourceBuilder.aggregations();*/
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        // 1.2 按照年龄段聚合
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        // 1.3 按照薪资，求平均薪资
        AvgAggregationBuilder balanceAgg = AggregationBuilders.avg("balanceAgg").field("balance");
        searchSourceBuilder.aggregation(ageAgg);
        searchSourceBuilder.aggregation(balanceAgg);
        // 检索条件
        searchRequest.source(searchSourceBuilder);
        // 2. 执行检索
        SearchResponse result = esClient.search(searchRequest, ElasticsearchConfig.COMMON_OPTIONS);
        System.out.println(searchSourceBuilder.toString());
        System.out.println(result.toString());
        // 3. 获取所有拿到的记录
        // 获取最外层的hit
        SearchHits hits = result.getHits();
        SearchHit[] hits1 = hits.getHits();
//        for (SearchHit hit: hits1) {
//            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
//            System.out.println(sourceAsMap);
//        }
        return result.toString();
    }

    /*TODO~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~redis分布式锁相关测试~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

    /**
     * @Description 使用嵌套查询，查询出各个年龄段的平均薪资
     *              一般用的多的就是嵌套查询，这里使用嵌套查询函很方便
     * @Author fly-ftx
     * @Date 16:53 2021/5/8
     * @Param []
     * @return void
     **/
    @GetMapping("/es/searchDataBuSubAggregation")
    public String searchDataBuSubAggregation() throws IOException {
        // 1. 创建检索请求
        SearchRequest searchRequest = new SearchRequest();
        /*设置索引，这里是在银行所以下完成测*/
        searchRequest.indices("bank");
        // 指定检索条件，这里的检索条件和在kibana很相似
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        // 1.2 按照年龄段聚合
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        // 1.3 按照薪资，求平均薪资
        AvgAggregationBuilder balanceAgg = AggregationBuilders.avg("balanceAgg").field("balance");
        ageAgg.subAggregation(balanceAgg);
        searchSourceBuilder.aggregation(ageAgg);
        // 检索条件
        searchRequest.source(searchSourceBuilder);
        // 2. 执行检索
        SearchResponse result = esClient.search(searchRequest, ElasticsearchConfig.COMMON_OPTIONS);
        System.out.println(searchSourceBuilder.toString());
        System.out.println(result.toString());
        // 3. 获取所有拿到的记录
        // 获取最外层的hit
        SearchHits hits = result.getHits();
        SearchHit[] hits1 = hits.getHits();
//        for (SearchHit hit: hits1) {
//            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
//            System.out.println(sourceAsMap);
//        }
        return result.toString();
    }

    @GetMapping("/redis/hello")
    public String hello() {
        // 1. 获取一把锁，只要锁的名字一样，就是同一把锁
        RLock lock = redisson.getLock("my-lock");
        // 2. 加锁,设置30秒过期，
        lock.lock(30 , TimeUnit.SECONDS);
        System.out.println("加锁成功，执行业务..." + Thread.currentThread().getId());
        // 1> TODO: 如果业务超长，redisson会自动续期30s，不用担心业务长，锁自动过期被删除
        // 2> TODO: 加锁的业务只要运行完成，就不会给当前锁续期，即使手动解锁，锁默认在30s以后自动删除
        /*
         *  lock.lock(10, TimeUnit.SECONDS);
         * 3> TODO: 如果我们传递了锁的超时时间，就发送给redis执行脚本，默认超时时间我们指定时间
         *     如果没有指定锁超时时间，就用默认的锁超时间。只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间，新的过期时间就是看门狗的默认时间】
         *     this.internalLockLeaseTime / 3L, TimeUnit.MILLISECONDS)---也就是每隔10秒自动进行续期
         *
         * 3> 最佳实战
         *    > TODO: 将过期时间设置长一点，对于长任务的，代器完成会自动删除锁。 这里就不用redisson内部执行自动续期操作
         *    lock.lock(30 , TimeUnit.SECONDS);
         *
         * */
        try {
            Thread.sleep(5000);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }finally {
            // 解锁
            System.out.println("释放锁..." + Thread.currentThread().getId());
            lock.unlock();
        }
        return "hello";
    }

    /**
     * @Description
     * TODO: 保证一定能够读到最新的数据，修改期间，写锁是一个排他锁，读锁是一个共享锁，
     *       写锁设置释放，读就必须等待
     *       写 + 读 等待写锁释放
     *       写 + 写 阻塞方式
     *       读 + 写 等待读锁释放
     *  只要有写的操作，都要等待
     * @Author fly-ftx
     * @Date 16:44 2021/5/10
     * @Param []
     * @return java.lang.String
     **/
    @GetMapping("/redis/write")
    public String write() {
        String s = "222222";
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = lock.writeLock();
        try {
            // TODO: 改数据加读=写锁，读数据加读锁
            rLock.lock(30, TimeUnit.SECONDS);
            System.out.println("写锁加锁成功..." + Thread.currentThread().getId() + " 加锁时间:"+LocalDateTime.now());
            Thread.sleep(5000);
            stringRedisTemplate.opsForValue().set("writeValue" , s);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }finally {
            rLock.unlock();
            System.out.println("写锁释放成功..." + Thread.currentThread().getId() + " 释放锁时间:"+LocalDateTime.now());
        }
        return s;
    }

    @GetMapping("/redis/read")
    public String read() {
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        // TODO: ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(); api是一样的，可以仿照
        String s = "";
        RLock rLock = lock.readLock();
        try {
            // TODO: 读数据加读锁
            rLock.lock();
            System.out.println("读锁加锁成功..." + Thread.currentThread().getId() + " 加锁时间:"+LocalDateTime.now());
            Thread.sleep(30000);
            s = stringRedisTemplate.opsForValue().get("writeValue");
        } catch (Exception exception) {
            exception.printStackTrace();
        }finally {
            rLock.unlock();
            System.out.println("读锁释放成功..." + Thread.currentThread().getId() + " 释放锁时间:"+LocalDateTime.now());
        }
        return s;
    }

    /**
     * @Description
     * TODO: 放假，锁门，等待所有班级都走了才能锁门
     *
     * @Author fly-ftx
     * @Date 17:00 2021/5/10
     * @Param
     * @return
     **/
    @GetMapping("/redis/lockDoor")
    public String lockDoor() {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        try {
            door.trySetCount(3 );
            System.out.println("开始等待班级走人...");
            door.await();
            System.out.println("班级都走光了，锁门...");
            return "放假了....";
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @GetMapping("/redis/leave")
    public String leave(@RequestParam String id) {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.countDown();
        System.out.println("第" + id + "班走人了") ;
        return "班级：" + id + "人走完了";
    }

    /**
     * @Description
     * TODO: 信号量
     * TODO: 场景：车库停车 3车位
     * TODO: 关于Semphore的使用，可以在限流的时候用这个，
     * @Author fly-ftx
     * @Date 17:25 2021/5/10
     * @Param
     * @return
     **/
    @GetMapping("/redis/park")
    public String park(@RequestParam String id) {
        RSemaphore park = redisson.getSemaphore("park");
        try {
            park.acquire();
            System.out.println("第" + id + "号开始停车");
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
        return "第" + id + "号开始停车";
    }

    @GetMapping("/redis/go")
    public String go(@RequestParam String id) {
        RSemaphore park = redisson.getSemaphore("park");
        park.release();
        System.out.println("第" + id + "已走" + "腾出一辆车位");
        return "第" + id + "已走" + "腾出一辆车位";
    }
}
