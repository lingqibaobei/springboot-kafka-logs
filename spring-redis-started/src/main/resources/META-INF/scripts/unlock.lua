--
-- Created by IntelliJ IDEA.
-- User: mac
-- Date: 2019-05-24
-- Time: 17:59
-- To change this template use File | Settings | File Templates.
--

if redis.call('get', KEYS[1]) == ARGV[1] then
    return redis.call('del', KEYS[1])
else
    return 0
end;


