--
-- Created by IntelliJ IDEA.
-- User: mac
-- Date: 2019-07-05
-- Time: 16:07
-- To change this template use File | Settings | File Templates.
--  @desc 计算器+1 Lua脚本
--  @params KEYS[1]  指定 key
--  @params ARGV[1]  指定 过期时间秒数
--  @return


if (redis.call('exists', KEYS[1]) == 1) then
    local current = tonumber(redis.call('GET', KEYS[1]));
    redis.call('SET', KEYS[1], current + 1, 'EX', ARGV[1])
    return tonumber(redis.call('GET', KEYS[1]));
end;
    redis.call('SET', KEYS[1], 1, 'EX', ARGV[1])
    return tonumber(redis.call('GET', KEYS[1]));