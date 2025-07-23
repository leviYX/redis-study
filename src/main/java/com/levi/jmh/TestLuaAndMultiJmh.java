package com.levi.jmh;
import com.levi.RedisClientFactory;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.All)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1)
@Measurement(iterations = 3)
@Fork(1)
public class TestLuaAndMultiJmh {

    private StatefulRedisConnection<String, String> conn;
    private RedisAsyncCommands<String, String> asyncCommandsLua;
    private RedisAsyncCommands<String, String> asyncCommandsMulti;

    private static final String LUA_SCRIPT =
            "local k=KEYS[1]; local t=ARGV[1]; local v=ARGV[2]; " +
                    "redis.call('HSET',k,t,v); redis.call('ZADD',k,t,v); return 0;";

    @Setup
    public void setup() {
        asyncCommandsLua = RedisClientFactory.getAsyncCommands();
        asyncCommandsMulti = RedisClientFactory.getAsyncCommands();
    }

    @TearDown
    public void tearDown() {
        conn.close();
    }

    @Benchmark
    public void lua() {
        String key  = "ts:device1:typeA";
        String ts   = String.valueOf(System.nanoTime());
        String val  = String.valueOf(ThreadLocalRandom.current().nextDouble());
        asyncCommandsLua.eval(LUA_SCRIPT, ScriptOutputType.INTEGER, key, ts, val);
    }

    @Benchmark
    public void multi() throws Exception {
        String key  = "ts:device1:typeB";
        String ts   = String.valueOf(System.nanoTime());
        String val  = String.valueOf(ThreadLocalRandom.current().nextDouble());
        double score = Double.parseDouble(ts);

        asyncCommandsMulti.multi().get();          // MULTI
        asyncCommandsMulti.hset(key, ts, val);
        asyncCommandsMulti.zadd(key, score, val);
        asyncCommandsMulti.exec().get();           // EXEC
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(TestLuaAndMultiJmh.class.getSimpleName())
                .output("TestLuaAndMultiJmh.json")
                .build();

        new Runner(opt).run();
    }
}
