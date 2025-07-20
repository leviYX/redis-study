package com.levi;

import com.levi.constant.RedisConstant;
import io.lettuce.core.RedisClient;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.ValueScanCursor;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

public class SetTest {

    private static RedisClient redisClient;

    private static StatefulRedisConnection<String, String> connection;

    private static RedisAsyncCommands<String, String> asyncCommands;

    private static RedisCommands<String, String> syncCommands;

    private static final String COLOR_PREFIX = "color#";


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
    public void testAdd() throws Throwable {
        for (int i = 0; i < 1000; i++) {
            syncCommands.sadd(COLOR_PREFIX + 1,"red" + i,"green" + i,"blue" + i);
        }
    }

    @Test
    public void testAddSets() throws Throwable {
        syncCommands.sadd(COLOR_PREFIX + 1,"red","green","blue");
        syncCommands.sadd(COLOR_PREFIX + 2,"red","purple","yellow");
        syncCommands.sadd(COLOR_PREFIX + 3,"red","black","blue");
    }

    @Test
    public void testUnionSets() throws Throwable {
        Set<String> sunion = syncCommands.sunion(COLOR_PREFIX + 1, COLOR_PREFIX + 2, COLOR_PREFIX + 3);
        System.out.println(sunion);
    }

    /**
     * sunionstore把并集结果存储到一个新的key中，我们这里指定为UNION_STORE
     *
     * @throws Throwable
     */
    @Test
    public void testUnionStoreSets() throws Throwable {
        Long sunionstore = syncCommands.sunionstore("UNION_STORE",COLOR_PREFIX + 1, COLOR_PREFIX + 2, COLOR_PREFIX + 3);
        System.out.println(sunionstore);
    }

    @Test
    public void testInterSets() throws Throwable {
        Set<String> sinter = syncCommands.sinter(COLOR_PREFIX + 1, COLOR_PREFIX + 2, COLOR_PREFIX + 3);
        System.out.println(sinter);
    }

    /**
     * diff的主体取决于第一个参数的key
     *
     * @throws Throwable
     */
    @Test
    public void testDiffSets() throws Throwable {
        Set<String> sdiff = syncCommands.sdiff(COLOR_PREFIX + 1, COLOR_PREFIX + 2, COLOR_PREFIX + 3);
        System.out.println(sdiff);
    }

    @Test
    public void testSisMember() throws Throwable {
        Boolean red = syncCommands.sismember(COLOR_PREFIX + 1, "red");
        String res = red?"是":"否";
        System.out.println("red是否存在于COLOR_PREFIX + 1" + res);
    }

    @Test
    public void testSCard() throws Throwable {
        Long scard = syncCommands.scard(COLOR_PREFIX + 1);
        System.out.println("COLOR_PREFIX + 1 中一共有" + scard + "个元素");
    }

    @Test
    public void testSrem() throws Throwable {
        // 移除black
        Long black = syncCommands.srem(COLOR_PREFIX + 1, "black");
    }

    @Test
    public void testSmembers() throws Throwable {
        // 获取COLOR_PREFIX + 1 中的所有元素，但是如果这个很大的话，会有big key的问题，所以一般我们用scan
        Set<String> smembers = syncCommands.smembers(COLOR_PREFIX + 1);
        System.out.println(smembers);
    }

    @Test
    public void testSrandmember() throws Throwable {
        // 随机获取COLOR_PREFIX + 1 中的一个元素
        String srandmember = syncCommands.srandmember(COLOR_PREFIX + 1);
        System.out.println(srandmember);
    }

    /**
     * 注意sscan是一个增量式的操作，每次调用sscan，会返回一个新的cursor，我们需要用这个新的cursor继续调用sscan，直到cursor.isFinished()为true
     * 但是每次获取的个数是不确定的，所以我们需要根据自己的业务场景来定，即便你指定了count，有时候也不会按照这个数字来获取
     * 尤其是当set中的元素个数比较小时，redis可能更加趋向于一次就全获取了。
     * 而且即便是set中元素个数很多，redis不会一次获取完毕，但是每一次批次获取可能也不是按照你的count来的
     */
    @Test
    public void testSscan() {
        ScanArgs scanArgs = ScanArgs.Builder.limit(2);
        ScanCursor cursor = ScanCursor.INITIAL;
        do {
            ValueScanCursor<String> scanCursor = syncCommands.sscan(COLOR_PREFIX + 1, cursor, scanArgs);
            System.out.println("当前批次获取到的元素: " + scanCursor.getValues());
            cursor = ScanCursor.of(scanCursor.getCursor());
            cursor.setFinished(scanCursor.isFinished());
        } while (!cursor.isFinished());
    }

}
