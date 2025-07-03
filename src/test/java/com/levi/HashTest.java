package com.levi;

import com.levi.constant.RedisConstant;
import com.levi.entity.User;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.MapUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class HashTest {

    private static RedisClient redisClient;

    private static StatefulRedisConnection<String, String> connection;

    private static RedisAsyncCommands<String, String> asyncCommands;

    private static Map<String, User> userDB = new HashMap<>();

    private static final String USER_CACHE_PREFIX = "uc_";

    private static final String CART_PREFIX = "cart_";




    @Before
    public void before() {
        redisClient = RedisClient.create(RedisConstant.REDIS_URL);
        connection = redisClient.connect();
        asyncCommands = connection.async();
        // RedisCommands<String, String> syncCommands = connection.sync();

        userDB.put("+8613912345678", new User(1L, "zhangsan", 25, "+8613912345678", "123456", "http://xxxx"));
        userDB.put("+8613512345678", new User(2L, "lisi", 25, "+8613512345678", "abcde", "http://xxxx"));
        userDB.put("+8618812345678", new User(3L, "wangwu", 25, "+8618812345678", "654321", "http://xxxx"));
        userDB.put("+8618912345678", new User(4L, "zhaoliu", 25, "+8618912345678", "98765", "http://xxxx"));
    }

    @After
    public void after() {
        connection.close();
        redisClient.shutdown();
    }

    @Test
    public void testUserCache() throws Throwable {
        mockLogin("+8613912345678", "654321");
        mockLogin("+8613912345678", "123456");
    }

    @Test
    public void testCartDao() throws Throwable {
        add(1024, "83694");
        add(1024, "1273979");
        add(1024, "123323");
        submitOrder(1024);
        remove(1024, "123323");
        incr(1024, "83694");
        decr(1024, "1273979");
    }

    private static void mockLogin(String mobile, String password) throws Exception {
        // 根据手机号，查询缓存
        String key = USER_CACHE_PREFIX + mobile;
        Map<String, String> userCache = asyncCommands.hgetall(key).get(1, TimeUnit.SECONDS);
        User user = null;
        if (MapUtils.isEmpty(userCache)) {
            System.out.println("缓存miss，加载DB");
            user = userDB.get(mobile);
            if (user == null) {
                System.out.println("登录失败");
                return;
            }
            // User转成Map
            Map<String, String> userMap = BeanUtils.describe(user);
            // 写入缓存
            Long result = asyncCommands.hset(key, userMap).get(1, TimeUnit.SECONDS);
            if (result == 1) {
                System.out.println("UserId:" + user.id() + "，已进入缓存");
            }
        } else {
            System.out.println("缓存hit");
            user = new User(UUID.randomUUID().timestamp(), "wangwu", 25, mobile, password, "http://xxxx");
            BeanUtils.populate(user, userCache);
        }
        if (password.equals(user.password())) {
            System.out.println(user.username() + ", 登录成功!");
        } else {
            System.out.println("登录失败");
        }

        System.out.println("================================");
    }

    public void add(long userId, String productId) throws Exception {
        Boolean result = asyncCommands.hset(CART_PREFIX + userId,
                productId, "1").get(1, TimeUnit.SECONDS);
        if (result) {
            System.out.println("添加购物车成功,productId:" + productId);
        }
    }

    public void remove(long userId, String productId) throws Exception {
        Long result = asyncCommands.hdel(CART_PREFIX + userId,
                productId).get(1, TimeUnit.SECONDS);
        if (result == 1) {
            System.out.println("商品删除成功，productId:" + productId);
        }
    }

    public void submitOrder(long userId) throws Exception {
        Map<String, String> cartInfo = asyncCommands.hgetall(CART_PREFIX + userId).get(1, TimeUnit.SECONDS);
        System.out.println("用户:"+userId+", 提交订单:");
        for (Map.Entry<String, String> entry : cartInfo.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
    }

    public void incr(long userId, String productId) throws Exception {
        Long result = asyncCommands.hincrby(CART_PREFIX + userId,
                productId, 1).get(1, TimeUnit.SECONDS);
        System.out.println("商品数量加1成功，剩余数量为:" + result);
    }

    public void decr(long userId, String productId) throws Exception {
        String count = asyncCommands.hget(CART_PREFIX + userId,
                productId).get(1, TimeUnit.SECONDS);
        if (Long.valueOf(count) - 1 <= 0) { // 删除商品
            remove(userId, productId);
            return;
        }
        Long result = asyncCommands.hincrby(CART_PREFIX + userId,
                productId, -1).get(1, TimeUnit.SECONDS);
        System.out.println("商品数量减1成功，剩余数量为:" + result);
    }
}
