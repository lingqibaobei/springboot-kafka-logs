package com.demo.redis.started.callback;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;

import java.util.Objects;

/**
 * lua script redis callback return int { @see ReturnType.INTEGER}
 *
 * @author Dean
 * @date 2021-01-12
 */
public class LuaScriptForIntegerRedisCallback implements RedisCallback<Boolean> {

    private String luaScript;

    private int keySize;

    private byte[][] keysAndArgs;

    public LuaScriptForIntegerRedisCallback(String unlockLuaScript, int keySize, byte[]... keysAndArgs) {
        this.luaScript = unlockLuaScript;
        this.keySize = keySize;
        this.keysAndArgs = keysAndArgs;
    }

    @Override
    public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
        Long eval = connection.eval(luaScript.getBytes(), ReturnType.INTEGER, keySize,
                keysAndArgs);
        return Objects.nonNull(eval) && eval > 0;
    }
}
