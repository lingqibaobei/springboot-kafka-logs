package com.demo.redis.started.sample;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import java.util.Collection;
import java.util.List;

/**
 * 优化hyperLogLog的pf count传递参数（可变参数，修改成list）
 *
 * @author fuhw/Dean
 * @date 2019-07-04
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisHyperLogLogTests {


    @Autowired
    private StringRedisTemplate redisTemplate;

    private String keyPrefix = "hello_";
    private int size = 10000;
    private List<String> hKeys = Lists.newArrayList();


    @Test
    public void testAddToHyperLogLog() {
        StopWatch countMonitor = new StopWatch();
        countMonitor.start();
        for (int i = 0; i < size; i++) {
            String key = keyPrefix + i;
            redisTemplate.opsForHyperLogLog().add(key, i + "");
        }
        countMonitor.stop();
        log.info("pfAdd[{}]条记录,总共消耗：{}(ms)", size, countMonitor.getTotalTimeMillis());

    }


    @Test
    public void testCountHyperLogLog() {
        for (int i = 0; i < size; i++) {
            String key = keyPrefix + i;
            Long size = redisTemplate.opsForHyperLogLog().size(key);
            if (size > 1) {
                log.info("数量大于1的key：{}", key);
            }
        }
    }

    @Test
    public void testPfCount() {
        for (int i = 0; i < size; i++) {
            String key = keyPrefix + i;
            hKeys.add(key);
        }
        StopWatch pfCountMonitor = new StopWatch();
        pfCountMonitor.start();
        long psCountResult = pfCount(hKeys);
        pfCountMonitor.stop();
        log.info("pfCount(优化前)[{}]条记录,总共消耗：{}(ms),统计结果：{}", size, pfCountMonitor.getTotalTimeMillis(), psCountResult);


        StopWatch pfMergeMonitor = new StopWatch();
        pfMergeMonitor.start();
        long pfMergeResult = pfMerge("new_hello".getBytes(), hKeys);
        pfMergeMonitor.stop();
        log.info("pfCount(PfMerge优化后)[{}]条记录,总共消耗：{}(ms),统计结果：{}", size, pfMergeMonitor.getTotalTimeMillis(), pfMergeResult);

    }

    public Long pfMerge(byte[] newKey, Collection<String> keys) {
        return redisTemplate.execute((RedisCallback<Long>) connection -> {
            byte[][] sourceKeys = rawKeys(keys);
            connection.pfMerge(newKey, sourceKeys);
            return connection.pfCount(newKey);
        });
    }


    private Long pfCount(Collection<String> keys) {
        return redisTemplate.execute((RedisCallback<Long>) connection ->
                connection.pfCount(rawKeys(keys)));
    }

    private static byte[][] rawKeys(Collection<String> keys) {
        final byte[][] rawKeys = new byte[keys.size()][];
        int i = 0;
        for (String k : keys) {
            rawKeys[i++] = k.getBytes();
        }
        return rawKeys;
    }
}
