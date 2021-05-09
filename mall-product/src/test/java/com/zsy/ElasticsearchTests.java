package com.zsy;

import com.zsy.product.config.ElasticsearchConfig;
import com.zsy.product.entity.BrandEntity;
import com.zsy.product.service.BrandService;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
class ElasticsearchTests {

    @Autowired
    RestHighLevelClient client;

    @Test
    void contextLoads() {
    }
    /**
     * @Description 查询数据-从elasticsearch
     *               其实这里的操作还是很简单的，之类和kibana是一样的操作，构造相应的条件就可以了
     * @Author fly-ftx
     * @Date 16:01 2021/5/8
     * @Param []
     * @return void
     **/
    @Test
    void searchData() throws IOException {
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
        SearchResponse result = client.search(searchRequest, ElasticsearchConfig.COMMON_OPTIONS);
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

    }


    /**
     * @Description 使用嵌套查询，查询出各个年龄段的平均薪资
     *              一般用的多的就是嵌套查询，这里使用嵌套查询函很方便
     * @Author fly-ftx
     * @Date 16:53 2021/5/8
     * @Param []
     * @return void
     **/
    @Test
    void searchDataBuSubAggregation() throws IOException {
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
        SearchResponse result = client.search(searchRequest, ElasticsearchConfig.COMMON_OPTIONS);
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

    }


}
