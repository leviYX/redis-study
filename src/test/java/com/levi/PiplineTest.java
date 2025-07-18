package com.levi;

import com.levi.constant.RedisConstant;
import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PiplineTest {

    private static RedisClient redisClient;

    private static StatefulRedisConnection<String, String> connection;

    private static RedisAsyncCommands<String, String> asyncCommands;


    @Before
    public void before() {
        redisClient = RedisClient.create(RedisConstant.REDIS_URL);
        connection = redisClient.connect();
        asyncCommands = connection.async();
        // RedisCommands<String, String> syncCommands = connection.sync();
    }

    @After
    public void after() {
        connection.close();
        redisClient.shutdown();
    }

    @Test
    public void testPipline() throws Throwable {
        // 关闭自动 flush
        asyncCommands.setAutoFlushCommands(false);
        List<RedisFuture<?>> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            futures.add(asyncCommands.hset("car" + i, Map.of("name", "car" + i, "color", "red")));
        }
        // 手动刷
        asyncCommands.flushCommands();

        // await
        boolean res = LettuceFutures.awaitAll(5, TimeUnit.SECONDS, futures.toArray(new RedisFuture[0]));
    }


}
