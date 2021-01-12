--
-- Created by IntelliJ IDEA.
-- User: mac
-- Date: 2019-05-24
-- Time: 18:00
-- To change this template use File | Settings | File Templates.

--  @desc 扣减库存Lua脚本 库存不足直接清零
--  @params KEYS[1]  指定 key
--  @params ARGV[1]  指定 field
--  @params ARGV[2]  库存扣减数量
--  @params ARGV[3]  当前时间戳
--  @return
-- 		-3:库存未初始化
-- 		-2:库存不足
-- 		-1:库存不存在
-- 		大于等于0:剩余库存（扣减之后剩余的库存）

if (redis.call('exists', KEYS[1]) == 1) then
    local stock = tonumber(redis.call('hget', KEYS[1], ARGV[1]));
    local num = tonumber(ARGV[2]);
    if (stock <= 0) then
        return -1;
    end;
    if (stock >= num) then
        redis.call('hset', KEYS[1], 'lastModifiedTime', ARGV[3])
        return redis.call('hincrby', KEYS[1], ARGV[1], 0 - num);
    else
        redis.call('hset', KEYS[1], ARGV[1], 0);
        redis.call('hset', KEYS[1], 'lastModifiedTime', ARGV[2]);
        return 0;
    end;
    return -2;
end;
return -3;