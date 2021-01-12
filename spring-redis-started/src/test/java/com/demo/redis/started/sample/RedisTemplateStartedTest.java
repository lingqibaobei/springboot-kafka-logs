package com.demo.redis.started.sample;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

/**
 * RedisTemplate测试
 *
 * @author fuhw/Dean
 * @date 2019-05-08
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTemplateStartedTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private String key;
    private String valueFrom;
    private String valueTo;

    @Before
    public void init() {
        key = "A";
        valueFrom = "10";
        valueTo = "20";
    }

    @Test
    public void testSetNxByTemplate() {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, valueFrom);
        String setResult = valueOperations.get(key);
        Assert.assertEquals(valueFrom, setResult);
        Boolean setIfAbsent = valueOperations.setIfAbsent(key, valueTo);
        Assert.assertEquals(Boolean.FALSE, setIfAbsent);
        redisTemplate.delete(key);
    }

    @Test
    public void testSetNxByExecute() {
        Boolean setResult = redisTemplate.execute((RedisCallback<Boolean>) connection ->
                connection.set(key.getBytes(), valueTo.getBytes(),
                        Expiration.from(10, TimeUnit.SECONDS),
                        RedisStringCommands.SetOption.SET_IF_ABSENT));
        Assert.assertEquals(Boolean.TRUE, setResult);
        String val = redisTemplate.opsForValue().get(key);
        Assert.assertNotNull(val);
        Assert.assertEquals(valueTo, val);

    }
}
