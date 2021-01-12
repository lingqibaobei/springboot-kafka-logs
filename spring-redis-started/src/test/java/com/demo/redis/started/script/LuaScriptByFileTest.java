package com.demo.redis.started.script;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

/**
 * Lua 脚本示例
 *
 * @author fuhw/Dean
 * @date 2019-05-07 11:18
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class LuaScriptByFileTest {


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    private String key = "A";
    private String keyA = "kA";
    private String keyB = "kB";
    private String newValue = "20";


    @After
    public void destroy() {
        redisTemplate.delete(Arrays.asList(key, keyA, keyB));
    }


    @Test
    public void testCheckAndSet() throws IOException {

        String oldValue = "10";
        // not match
        ScriptSource scriptSource = new ResourceScriptSource(new ClassPathResource("META-INF/scripts/check_and_set.lua"));
        final RedisScript<Boolean> script = RedisScript.of(scriptSource.getScriptAsString(), Boolean.class);
        Boolean checkAndSetResult = redisTemplate.execute(script, Collections.singletonList(key), oldValue, newValue);
        Assert.assertEquals(Boolean.FALSE, checkAndSetResult);
        // match
        redisTemplate.opsForValue().set(key, oldValue);
        Boolean execute = redisTemplate.execute(script, Collections.singletonList(key), oldValue, newValue);
        Assert.assertEquals(Boolean.TRUE, execute);
        redisTemplate.delete(key);

        // 等同于 eval
//        Boolean checkAndSetResult = redisTemplate.execute(new RedisCallback<Boolean>() {
//            @Override
//            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
//                Long eval = connection.eval(scriptAsString.getBytes(), ReturnType.INTEGER, 1, key.getBytes(), oldValue.getBytes(), newValue.getBytes());
//                System.out.println("eval结果:" + eval);
//                return Objects.isNull(eval) ? false : eval >= 1;
//            }
//        });

        // 等同于 evalSha
//        Boolean checkAndSetResult = redisTemplate.execute(new RedisCallback<Boolean>() {
//            @Override
//            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
//                Long eval = connection.evalSha(DigestUtils.sha1DigestAsHex(scriptAsString), ReturnType.INTEGER, 1, key.getBytes(), oldValue.getBytes(), newValue.getBytes());
//                System.out.println("eval结果:" + eval);
//                return Objects.isNull(eval) ? false : eval >= 1;
//            }
//        });

    }

    @Test
    public void testDecrInventoryByLua() throws IOException {
        String field = "num";
        String decrCount = "4";
        // not init inventory count
        ScriptSource scriptSource = new ResourceScriptSource(new ClassPathResource("META-INF/scripts/decr_inventory.lua"));
        String scriptAsString = scriptSource.getScriptAsString();
        final RedisScript<Long> decrLua = RedisScript.of(scriptAsString, Long.class);
        String nowDateStr = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        Long descNotInit = redisTemplate.execute(decrLua, Collections.singletonList(key), field, decrCount, nowDateStr);
        Assert.assertNotNull(descNotInit);
        Assert.assertEquals(Long.valueOf(-3), descNotInit);

        // after init inventory count
        String initCount = "100";
        redisTemplate.opsForHash().put(key, field, initCount);

        Long assertDecrResult = Long.valueOf(initCount) - Long.valueOf(decrCount);
        Long decrResult = redisTemplate.execute(decrLua, Collections.singletonList(key), field, decrCount, nowDateStr);
        Assert.assertNotNull(decrResult);
        Assert.assertEquals(assertDecrResult, decrResult);

    }


    /**
     * pipeline 使用同一个session链接（pipeline不是原子性的，中间可能会存在部分失败）
     */
    @Test
    public void testPipeline() {

        //  pipeline 异常不回滚
        try {
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                connection.del(keyA.getBytes(), keyB.getBytes());
                connection.set(keyA.getBytes(), newValue.getBytes());
                int a = 1 / 0;
                connection.set(keyB.getBytes(), newValue.getBytes());
                return null;
            });
        } catch (Exception e) {
            Object setAEx = redisTemplate.opsForValue().get(keyA);
            Assert.assertNotNull(setAEx);
            Assert.assertEquals(newValue, setAEx);

            Object setBEx = redisTemplate.opsForValue().get(keyB);
            Assert.assertNull(setBEx);
        }


        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.del(keyA.getBytes(), keyB.getBytes());
            connection.set(keyA.getBytes(), newValue.getBytes());
            connection.set(keyB.getBytes(), newValue.getBytes());
            return null;
        });
        Object oA = redisTemplate.opsForValue().get(keyA);
        Object oB = redisTemplate.opsForValue().get(keyB);
        Assert.assertEquals(oA, oB);
    }


    /**
     * 事务要在同一个session链接才能生效
     * 注意： 集群模式的redis不支持事务,事务也不是原子性的，中间可能会存在部分失败,
     * 比如：程序正常，执行命令发出去之后，redis异常执行
     */
    @Test
    public void testTransaction() {

        try {
            redisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                public Object execute(RedisOperations operations) throws DataAccessException {
                    operations.multi();
                    operations.delete(keyA);
                    operations.delete(keyB);
                    operations.opsForValue().set(keyA, newValue);
                    int a = 1 / 0;
                    operations.opsForValue().set(keyB, newValue);
                    operations.exec();
                    return null;
                }
            });
        } catch (Exception e) {
            Object setAEx = redisTemplate.opsForValue().get(keyA);
            Assert.assertNull(setAEx);

            Object setBEx = redisTemplate.opsForValue().get(keyB);
            Assert.assertNull(setBEx);
        }


        redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                operations.delete(keyA);
                operations.delete(keyB);
                operations.opsForValue().set(keyA, newValue);
                operations.opsForValue().set(keyB, newValue);
                operations.exec();
                return null;
            }

        });
        Object oA = redisTemplate.opsForValue().get(keyA);
        Object oB = redisTemplate.opsForValue().get(keyB);
        Assert.assertEquals(oA, oB);
    }
}
