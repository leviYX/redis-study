package com.levi;

import com.levi.constant.RedisConstant;
import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class ListTest {

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
    public void MQProducerTest() {
        while (true) {
            try {
                Thread.sleep(1000L);
                String element = "element" + System.currentTimeMillis();
                Long size = asyncCommands.lpush("mq-test", element).get(1, TimeUnit.SECONDS);
                System.out.println("Producer:写入" + element + "，当前MQ长度:" + size);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void MQConsumerTest() {
        while (true) {
            try {
                KeyValue<String, String> keyValue = asyncCommands.brpop(1, "mq-test").get(2, TimeUnit.SECONDS);
                if (keyValue != null && keyValue.hasValue()) {
                    System.out.println("Consumer:从【" + keyValue.getKey() + "】队列中消费到【" + keyValue.getValue() + "】元素");
                } else {
                    System.out.println("Consumer:没有监听到任何数据，继续监听");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testLikeList() throws Exception {
        long videoId = 1089;
        final String listName = "like-list-" + videoId;
        Thread createLike = new Thread(() -> {
            for (int i = 0; i < 1000000; i++) {
                try {
                    // 不断有小伙伴点赞
                    asyncCommands
                            .rpush(listName, String.valueOf(i))
                            .get(1, TimeUnit.SECONDS);
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1, 2) * 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        createLike.start();

        Thread kz = new Thread(() -> {
            while (true) {
                try {
                    // 拉取前1000个点赞
                    List<String> userIds = asyncCommands
                            .lrange(listName, 0, 1000)
                            .get(1, TimeUnit.SECONDS);
                    Thread.sleep(2000);// 2秒钟拉取最新的点赞信息
                    System.out.println("小伙伴:[" + userIds + "]点赞了视频" + videoId);
                    // 删除已经拉取到的点赞信息
                    String s = asyncCommands.ltrim(listName, userIds.size(), -1)
                            .get(1, TimeUnit.SECONDS);
                    if("OK".equals(s)){
                        System.out.println("删除成功");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        kz.start();
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void testHotList() throws Exception {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        asyncCommands.del("hot-list").get(1, TimeUnit.SECONDS);
        // 初始化热点列表
        for (int i = 0; i < 10; i++) {
            asyncCommands.rpush("hot-list", "第" + i + "条热点新闻，更新时间:" + dateTimeFormatter.format(LocalDateTime.now()));
        }

        // 启动更新hotList的线程，模拟热点新闻的更新操作
        Thread changeHotList = new Thread(() -> {
            while (true) {
                try {
                    int index = ThreadLocalRandom.current().nextInt(0, 10);
                    asyncCommands.lset("hot-list", index, "第" + index + "条热点新闻，更新时间:" + dateTimeFormatter.format(LocalDateTime.now()));
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        changeHotList.start();

        // 模拟客户端更新热点新闻的拉取操作
        while (true) {
            List<String> hotList = asyncCommands.lrange("hot-list", 0, -1)
                    .get(1, TimeUnit.SECONDS);
            Thread.sleep(5000);// 2秒钟拉取最新的点赞信息
            System.out.println("热点新闻:");
            for (String hot : hotList) {
                System.out.println(hot);
            }
            System.out.println("=========");
        }
    }


}
