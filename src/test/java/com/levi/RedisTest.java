package com.levi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levi.entity.Product;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.concurrent.TimeUnit;

public class RedisTest {

    private static RedisClient redisClient;

    private static StatefulRedisConnection<String, String> connection;

    private static RedisAsyncCommands<String, String> asyncCommands;

    @Before
    public void before(){
        redisClient = RedisClient.create("redis://123456@127.0.0.1:6379/0");
        connection = redisClient.connect();
        asyncCommands = connection.async();
        // RedisCommands<String, String> syncCommands = connection.sync();
    }

    @After
    public void after(){
        connection.close();
        redisClient.shutdown();
    }

    @Test
    public void testCacheProduct() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Product product = new Product("杯子", 100d, "这是一个杯子");
        String json = objectMapper.writeValueAsString(product);
        asyncCommands.set("product", json).get(1, TimeUnit.SECONDS);
    }

    @Test
    public void testGetProduct() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = asyncCommands.get("product").get(1, TimeUnit.SECONDS);
        Product product = objectMapper.readValue(json, new TypeReference<Product>() {});
        System.out.println(product);
    }
}
