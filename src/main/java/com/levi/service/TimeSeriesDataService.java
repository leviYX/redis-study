package com.levi.service;

import com.levi.RedisClientFactory;
import com.levi.param.AddTimeSeriesDataParam;
import com.levi.utils.RedisKeysUtil;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.TransactionResult;

public class TimeSeriesDataService {

    private static final String LUA_TS_ADD =
            "local k=KEYS[1]; local t=ARGV[1]; local v=ARGV[2]; " +
                    "redis.call('HSET',k,t,v); redis.call('ZADD',k,t,v); return 0;";

    public boolean addTimeSeriesDataByLua(AddTimeSeriesDataParam param){
        final var deviceName = param.getDeviceName();
        final var type = param.getType();
        final var ts = param.getTimeStamp();
        final var value = String.valueOf(param.getValue());
        final var key = RedisKeysUtil.buildTimeSeriesKey(deviceName, type);
        try {
            var sync = RedisClientFactory.getSyncCommands();
            sync.eval(LUA_TS_ADD, ScriptOutputType.INTEGER, key, ts, value);
            return Boolean.TRUE;
        } catch (Exception e) {
            return Boolean.FALSE;
        } finally {
            RedisClientFactory.close();
        }
    }

    public boolean addTimeSeriesDataByTransaction(AddTimeSeriesDataParam param) {
        final var deviceName = param.getDeviceName();
        final var type = param.getType();
        final var timeStamp = param.getTimeStamp();
        final var value = String.valueOf(param.getValue());

        String key = RedisKeysUtil.buildTimeSeriesKey(deviceName, type);
        try {
            var async = RedisClientFactory.getAsyncCommands();   // 自己封装拿连接
            // 开启事务MULTI,然后阻塞等待
            async.multi().get();
            // 把两条命令入队
            async.hset(key, timeStamp, value);
            async.zadd(key, Double.parseDouble(timeStamp), value);
            // 原子提交EXEC
            TransactionResult execRet = async.exec().get();
            // EXEC 正常返回时，事务一定全部成功
            return Boolean.TRUE;
        } catch (Exception e) {
            // 发生异常时：
            //   1) MULTI 之后的命令不会真正执行
            //   2) 如果已经 MULTI 但没 EXEC，Redis 会自动 DISCARD
            e.printStackTrace();
            return Boolean.FALSE;
        } finally {
            RedisClientFactory.close();
        }
    }
}
