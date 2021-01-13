package com.demo.redis.started.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁
 * 使用 SET resource-name anystring NX EX max-lock-time 实现
 * <p>
 * 该方案在 Redis 官方 SET 命令页有详细介绍。
 * http://doc.redisfans.com/string/set.html
 * <p>
 * 在介绍该分布式锁设计之前，我们先来看一下在从 Redis 2.6.12 开始 SET 提供的新特性，
 * 命令 SET key value [EX seconds] [PX milliseconds] [NX|XX]，其中：
 * <p>
 * EX seconds — 以秒为单以毫秒为单位设置位设置 key 的过期时间；
 * PX milliseconds —  key 的过期时间；
 * NX — 将key 的值设为value ，当且仅当key 不存在，等效于 SETNX。
 * XX — 将key 的值设为value ，当且仅当key 存在，等效于 SETEX。
 * <p>
 * 命令 SET resource-name anystring NX EX max-lock-time 是一种在 Redis 中实现锁的简单方法。
 * <p>
 * 客户端执行以上的命令：
 * <p>
 * 如果服务器返回 OK ，那么这个客户端获得锁。
 * 如果服务器返回 NIL ，那么客户端获取锁失败，可以在稍后再重试。
 *
 * @author fuhw/dean
 * @date 2019-05-06
 */
public class RedisTemplateLock {

    private static Logger log = LoggerFactory.getLogger(RedisTemplateLock.class);

    private RedisTemplate<String, Object> redisTemplate;
    /**
     * 默认请求锁的等待超时时间(ms)
     */
    private static final long DEFAULT_LOCK_WAIT_TIME_OUT = 100;

    /**
     * 默认锁的有效时间(s)
     */
    private static final int DEFAULT_LOCK_EXPIRE_TIME = 60;

    /**
     * 解锁的lua脚本
     */
    private static final String UNLOCK_LUA =
            " if redis.call( 'get' ,KEYS[1]) == ARGV[1] " +
                    " then " +
                    "    return redis.call( 'del' ,KEYS[1]) " +
                    " else " +
                    "    return 0 " +
                    " end ";

    /**
     * 锁标志对应的key
     */
    private String lockKey;

    /**
     * 锁对应的值
     */
    private String lockValue;

    /**
     * 锁的有效时间(s)
     */
    private int lockExpireTime = DEFAULT_LOCK_EXPIRE_TIME;

    /**
     * 锁等待超时时间(ms)
     */
    private long lockWaitTimeout = DEFAULT_LOCK_WAIT_TIME_OUT;

    /**
     * 锁状态
     */
    private volatile boolean locked = false;

    /**
     * 锁标记后缀
     */
    private static final String LOCK_SUFFIX = "_lock";

    private Random random = new Random();

    /**
     * 使用默认的锁过期时间和请求锁的超时时间
     *
     * @param redisTemplate RedisTemplate<String, Object>
     * @param lockKey 锁的key（Redis的Key）
     */
    public RedisTemplateLock(RedisTemplate<String, Object> redisTemplate, String lockKey) {
        this.redisTemplate = redisTemplate;
        this.lockKey = String.join("", lockKey, LOCK_SUFFIX);
    }

    /**
     * 使用默认的请求锁的超时时间，指定锁的过期时间
     *
     * @param redisTemplate RedisTemplate<String, Object>
     * @param lockKey 锁的key（Redis的Key）
     * @param expireTime 锁的过期时间(单位：秒)
     */
    public RedisTemplateLock(RedisTemplate<String, Object> redisTemplate, String lockKey, int expireTime) {
        this(redisTemplate, lockKey);
        this.lockExpireTime = expireTime;
    }

    /**
     * 使用默认的锁的过期时间，指定请求锁的超时时间
     *
     * @param redisTemplate RedisTemplate<String, Object>
     * @param lockKey 锁的key（Redis的Key）
     * @param timeOut 请求锁的超时时间(单位：毫秒)
     */
    public RedisTemplateLock(RedisTemplate<String, Object> redisTemplate, String lockKey, long timeOut) {
        this(redisTemplate, lockKey);
        this.lockWaitTimeout = timeOut;
    }

    /**
     * 锁的过期时间和请求锁的超时时间都是用指定的值
     *
     * @param redisTemplate RedisTemplate<String, Object>
     * @param lockKey 锁的key（Redis的Key）
     * @param expireTime 锁的过期时间(单位：秒)
     * @param timeOut 请求锁的超时时间(单位：毫秒)
     */
    public RedisTemplateLock(RedisTemplate<String, Object> redisTemplate, String lockKey, int expireTime, long timeOut) {
        this(redisTemplate, lockKey, expireTime);
        this.lockWaitTimeout = timeOut;
    }

    /**
     * 尝试获取锁 超时返回
     *
     * @return true/false
     */
    public boolean tryLock() {
        // 生成随机key
        lockValue = UUID.randomUUID().toString();
        // 请求锁超时时间，纳秒
        long timeout = TimeUnit.NANOSECONDS.convert(lockWaitTimeout, TimeUnit.MILLISECONDS);
        // 请求开始时间，纳秒
        long startTime = System.nanoTime();
        while ((System.nanoTime() - startTime) < timeout) {
            if (Boolean.TRUE.equals(this.set(lockKey, lockValue, lockExpireTime))) {
                locked = true;
                return true;
            }
            // 每次请求等待一段时间
            sleep(10);
        }
        return locked;
    }

    /**
     * 尝试获取锁 立即返回
     *
     * @return 是否成功获得锁
     */
    public boolean lock() {
        lockValue = UUID.randomUUID().toString();
        //不存在则添加 且设置过期时间（单位ms）
        locked = this.set(lockKey, lockValue, lockExpireTime);
        return locked;
    }

    /**
     * 以阻塞方式的获取锁
     *
     * @return 是否成功获得锁
     */
    public boolean lockBlock() {
        lockValue = UUID.randomUUID().toString();
        while (true) {
            //不存在则添加 且设置过期时间（单位ms）
            if (Boolean.TRUE.equals(this.set(lockKey, lockValue, lockExpireTime))) {
                locked = true;
                return locked;
            }
            // 每次请求等待一段时间
            sleep(10);
        }
    }

    /**
     * 解锁
     * <p>
     * 可以通过以下修改，让这个锁实现更健壮：
     * <p>
     * <ul>不使用固定的字符串作为键的值，而是设置一个不可猜测（non-guessable）的长随机字符串，作为口令串（token）</ul>
     * <ul>不使用 DEL 命令来释放锁，而是发送一个 Lua 脚本，这个脚本只在客户端传入的值和键的口令串相匹配时，才对键进行删除</ul>
     * 这两个改动可以防止持有过期锁的客户端误删现有锁的情况出现。
     */
    public Boolean unlock() {
        // 只有加锁成功并且锁还有效才去释放锁
        return redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                Long result = connection.eval(UNLOCK_LUA.getBytes(), ReturnType.INTEGER, 1, lockKey.getBytes(), lockValue.getBytes());
                locked = Objects.nonNull(result) && result <= 0;
                if (locked) {
                    log.warn("释放锁[{}]失败！解锁时间：{}", lockKey, System.currentTimeMillis());
                } else {
                    log.info("释放锁[{}]成功！解锁时间：{}", lockKey, System.currentTimeMillis());
                }
                return !locked;
            }
        });
    }

    /**
     * 获取锁状态
     */
    public boolean isLock() {
        return locked;
    }

    /**
     * 重写redisTemplate的set方法
     * <p>
     * 命令 SET resource-name any string NX EX max-lock-time 是一种在 Redis 中实现锁的简单方法。
     * NX - 将key 的值设为value ，当且仅当key 不存在，等效于 SETNX。
     * EX - 以秒为单位设置 key 的过期时间，等效于EXPIRE key seconds
     * <p>
     * 客户端执行以上的命令：
     * <p>
     * 如果服务器返回 TRUE ，那么这个客户端获得锁。
     * 如果服务器返回 FALSE ，那么客户端获取锁失败，可以在稍后再重试。
     *
     * @param key 锁的Key
     * @param value 锁里面的值
     * @param seconds 过期时间（秒）
     * @return true/false
     */
    private Boolean set(final String key, final String value, final long seconds) {
        Assert.isTrue(!StringUtils.isEmpty(key), "key must be not null");
        Assert.isTrue(!StringUtils.isEmpty(value), "value must be not null");
        return redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                Boolean setResult = connection.set(key.getBytes(), value.getBytes(),
                        Expiration.from(seconds, TimeUnit.SECONDS),
                        RedisStringCommands.SetOption.SET_IF_ABSENT);
                if (Boolean.TRUE.equals(setResult)) {
                    log.info("获取锁[{}]的时间：{}", lockKey, System.currentTimeMillis());
                }
                return setResult;
            }
        });
    }

    /**
     * 线程等待时间
     *
     * @param millis 毫秒
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis, random.nextInt(999999));
        } catch (InterruptedException e) {
            log.error("获取分布式锁休眠被中断：", e);
        }
    }

    public String getLockKey() {
        return lockKey;
    }

    public int getLockExpireTime() {
        return lockExpireTime;
    }

    public void setLockExpireTime(int lockExpireTime) {
        this.lockExpireTime = lockExpireTime;
    }

    public long getLockWaitTimeout() {
        return lockWaitTimeout;
    }

    public void setLockWaitTimeout(long lockWaitTimeout) {
        this.lockWaitTimeout = lockWaitTimeout;
    }
}