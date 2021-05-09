package com.zsy.product.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch配置类
 * 给容器注入
 *
 * @author fly-ftx
 * @version 1.0
 * @date 2021/5/3 09:58
 */
@Configuration
@RefreshScope
public class ElasticsearchConfig {
    @Value(value = "${spring.elasticsearch.host}")
    private  String host;
    @Value(value = "${spring.elasticsearch.port}")
    private int port;
    @Value(value = "${spring.elasticsearch.scheme}")
    private String scheme;


    public static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
//        builder.addHeader("Authorization", "Bearer " + TOKEN);
//        builder.setHttpAsyncResponseConsumerFactory(
//                new HttpAsyncResponseConsumerFactory
//                        .HeapBufferedResponseConsumerFactory(30 * 1024 * 1024 * 1024));
        COMMON_OPTIONS = builder.build();
    }
    @Bean
    public RestHighLevelClient esRestClient() {
        HttpHost httpHost = new HttpHost(host, port , scheme);
        RestClientBuilder builder = RestClient.builder(httpHost);
        return new RestHighLevelClient(builder);
    }
}
