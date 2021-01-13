package com.demo.redis.started.sample;

import com.demo.redis.started.utils.RedisTemplateLock;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义分布式锁的测试
 *
 * @author fuhw/Dean
 * @date 2019-05-06
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTemplateLockTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 尝试加锁，释放锁
     */
    @Test
    public void testLockAndUnLock() {
        RedisTemplateLock inventoryLock = new RedisTemplateLock(redisTemplate, "lock_no");
        inventoryLock.lock();
        inventoryLock.unlock();
    }

    /**
     * 加锁（多线程）
     */
    @Test
    public void testLockByMultiThread() throws InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 20; i++) {
            final int curIndex = i;
            executorService.execute(() -> {
                RedisTemplateLock inventoryLock = new RedisTemplateLock(redisTemplate, "lock_no" + curIndex);
                inventoryLock.tryLock();
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                inventoryLock.unlock();
            });
        }
        TimeUnit.MILLISECONDS.sleep(10000);

    }

    /**
     * 加锁，释放锁（并发：同一把锁)
     * <p>
     * a.tryLock() 获取锁的数量，跟归还锁的数量一致,部分失败并且锁等待超时的会失败
     * a.lockBlock() 获取锁的数量，跟归还锁的数量一致,全部成功获取锁
     */
    @Test
    public void testLockConcurrent() throws InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        // 获取锁的数量
        AtomicInteger getLockCount = new AtomicInteger();
        // 归还锁的数量
        AtomicInteger returnLockCount = new AtomicInteger();

        String lockKey = "lock_no";
        for (int i = 0; i < 10000; i++) {
            executorService.execute(() -> {
                RedisTemplateLock inventoryLock = new RedisTemplateLock(redisTemplate, lockKey);
                boolean lockResult = inventoryLock.tryLock();
                if (lockResult) {
                    getLockCount.incrementAndGet();
                }
                Boolean unlockResult = inventoryLock.unlock();
                if (unlockResult) {
                    returnLockCount.incrementAndGet();
                }
            });
        }
        TimeUnit.MILLISECONDS.sleep(2000);
        Assert.assertEquals(getLockCount.intValue(), returnLockCount.intValue());
    }

}
