package com.levi;

import com.levi.constant.RedisConstant;
import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class SortSetTest {

    private static RedisClient redisClient;

    private static StatefulRedisConnection<String, String> connection;

    private static RedisAsyncCommands<String, String> asyncCommands;

    private static RedisCommands<String, String> syncCommands;

    private static final String COLOR_PREFIX = "color#sz";


    @Before
    public void before() {
        redisClient = RedisClient.create(RedisConstant.REDIS_URL);
        connection = redisClient.connect();
        asyncCommands = connection.async();
        syncCommands = connection.sync();
    }

    @After
    public void after() {
        connection.close();
        redisClient.shutdown();
    }

    @Test
    public void testAdd() {
        syncCommands.zadd(COLOR_PREFIX,10,"red");
        syncCommands.zadd(COLOR_PREFIX,20,"green");
        syncCommands.zadd(COLOR_PREFIX,30,"blue");
        syncCommands.zadd(COLOR_PREFIX,40,"yellow");
        syncCommands.zadd(COLOR_PREFIX,50,"black");
        syncCommands.zadd(COLOR_PREFIX,60,"white");
    }

    @Test
    public void testGetScore() {
        Double redScore = syncCommands.zscore(COLOR_PREFIX, "red");
        System.out.println(redScore);
    }

    @Test
    public void testRemove() {
        Long red = syncCommands.zrem(COLOR_PREFIX, "red");
        System.out.println(red);
    }

    @Test
    public void testCount() {
        Long allCount = syncCommands.zcard(COLOR_PREFIX);
        System.out.println("key里面的总数是:" + allCount);

        Long count = syncCommands.zcount(COLOR_PREFIX, 10, 30);
        System.out.println("大于等于10，小于等于30的总个数是:" + count);

        Range range = Range.create(10, 30);
        Long zcount = syncCommands.zcount(COLOR_PREFIX, range);
        System.out.println("大于等于10，小于等于30的总个数是:" + zcount);
    }

    @Test
    public void testRemMaxOrMin() {
        // 移除分数最大的
        ScoredValue<String> zpopmax = syncCommands.zpopmax(COLOR_PREFIX);
        // 移除分数最小的
        ScoredValue<String> zpopmin = syncCommands.zpopmin(COLOR_PREFIX);

    }

    @Test
    public void testIncrBy() {
        // 为其中一个key的分数+10
        syncCommands.zincrby(COLOR_PREFIX, 10, "blue");
        // 为其中一个key的分数-10
        syncCommands.zincrby(COLOR_PREFIX,-10,"yellow");
    }

    @Test
    public void testRange() {
        // 获取 分数为10 - 30之间的元素
        List<String> zrange = syncCommands.zrange(COLOR_PREFIX, 1, 2);
        System.out.println(zrange.toString());

        List<ScoredValue<String>> scoredValues = syncCommands.zrangeWithScores(COLOR_PREFIX, 1,2);
        System.out.println(scoredValues.toString());
    }


}
