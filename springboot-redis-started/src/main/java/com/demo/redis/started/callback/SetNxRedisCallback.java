package com.demo.redis.started.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.types.Expiration;

import java.util.concurrent.TimeUnit;

/**
 * redis 的set的回调
 * <p>默认过期时间<p/>
 *
 * @author fuhw/dean
 * @date 2019-05-07
 */
@Slf4j
public class SetNxRedisCallback implements RedisCallback<Boolean> {

    /**
     * 默认value的过期时间 (s)
     */
    private static final int DEFAULT_VALUE_EXPIRE_TIME = 60;

    private static final String EMPTY_VALUE = "";
    /**
     * redis key
     */
    private String key;

    /**
     * redis value
     */
    private String value;

    /**
     * 过期时间，单位秒
     */
    private int expireTime;

    public SetNxRedisCallback(String key) {
        this(key, DEFAULT_VALUE_EXPIRE_TIME);
    }

    public SetNxRedisCallback(String key, int expireTime) {
        this(key, EMPTY_VALUE, expireTime);
    }

    public SetNxRedisCallback(String key, String value, int expireTime) {
        this.key = key;
        this.value = value;
        this.expireTime = expireTime;
    }

    @Override
    public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
        return connection.set(key.getBytes(),
                value.getBytes(),
                Expiration.from(expireTime, TimeUnit.SECONDS),
                RedisStringCommands.SetOption.SET_IF_ABSENT);
    }
}
