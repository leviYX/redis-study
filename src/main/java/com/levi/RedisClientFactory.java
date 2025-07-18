package com.levi;

import com.levi.constant.RedisConstant;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;

public class RedisClientFactory {


    private static RedisClient redisClient;

    private static StatefulRedisConnection<String, String> connection;

    private static RedisAsyncCommands<String, String> asyncCommands;

    private static RedisCommands<String, String> syncCommands;

    static {
        redisClient = RedisClient.create(RedisConstant.REDIS_URL);
        connection = redisClient.connect();
        asyncCommands = connection.async();
        syncCommands = connection.sync();
    }

    public static RedisAsyncCommands<String, String> getSyncCommands() {
        return asyncCommands;
    }

    public static RedisAsyncCommands<String, String> getAsyncCommands() {
        return asyncCommands;
    }

    public static void close() {
        connection.close();
        redisClient.shutdown();
    }
}
