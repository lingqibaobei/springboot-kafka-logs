--
-- Created by IntelliJ IDEA.
-- User: fuhw/Dean
-- Date: 2019-05-24
-- Time: 17:32
-- To change this template use File | Settings | File Templates.
--
-- @param KEYS[1] 指定key
-- @param ARGV[1] 原始value
-- @param ARGV[2] 新value
-- @return boolean
local current = redis.call('GET', KEYS[1])
if current == ARGV[1] then redis.call('SET', KEYS[1], ARGV[2])
    return true;
end
return false;