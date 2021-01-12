package com.demo.redis.started.script;

import com.demo.redis.started.callback.LuaScriptForIntegerRedisCallback;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Lua 脚本示例
 *
 * @author fuhw/Dean
 * @date 2019-05-07 11:18
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class LuaScriptStartedTest {


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PRINT_LUA = "return {KEYS[1],KEYS[2],ARGV[1],ARGV[2]}";

    @Test
    public void testLuaScriptPrint() {
        String keyA = "A";
        String keyB = "B";
        String valA = "10";
        String valB = "20";
        RedisScript<List> script = new DefaultRedisScript<>(PRINT_LUA, List.class);
        Object result = redisTemplate.execute(script,
                Arrays.asList(keyA, keyB), valA, valB);
        Assert.assertNotNull(result);
        Assert.assertEquals(Arrays.asList(keyA, keyB, valA, valB), result);
    }

    /**
     * 注意：
     * execute的参数类型 Object... args
     * lua脚本中执行 redis.call 返回的结果类型是不固定的，
     * 示例：
     * 成功返回OK
     */
    private static final String CALL_LUA = "return redis.call('set',KEYS[1],ARGV[1])";

    @Test
    public void testLuaScriptCall() {
        String key = "A";
        String val = "110";
        RedisScript<String> script = new DefaultRedisScript<>(CALL_LUA, String.class);
        String result = redisTemplate.execute(script,
                Collections.singletonList(key), val);
        Assert.assertNotNull(result);
        Assert.assertEquals("OK", result);
        Boolean delete = redisTemplate.delete(key);
        Assert.assertEquals(Boolean.TRUE, delete);

    }


    /**
     * 如果key和value匹配，则删除，反之，返回0
     */
    private static final String UNLOCK_LUA;

    static {
        UNLOCK_LUA = " if redis.call( 'get' ,KEYS[1]) == ARGV[1] " +
                " then " +
                "    return redis.call( 'del' ,KEYS[1]) " +
                " else " +
                "    return 0 " +
                " end ";
    }

    @Test
    public void testLuaScriptUnlockByEval() {
        String lockKey = "A";
        String lockValue = "111";
        // no lockKey
        Boolean exeOne = redisTemplate.execute(new LuaScriptForIntegerRedisCallback(UNLOCK_LUA, 1, lockKey.getBytes(), lockValue.getBytes()));
        Assert.assertEquals(Boolean.FALSE, exeOne);

        // add lockKey
        redisTemplate.opsForValue().set(lockKey, lockValue, Duration.ofSeconds(10));

        // get lockKey and remove
        Boolean exeTow = redisTemplate.execute(new LuaScriptForIntegerRedisCallback(UNLOCK_LUA, 1, lockKey.getBytes(), lockValue.getBytes()));
        Assert.assertEquals(Boolean.TRUE, exeTow);

    }


}
