-- 删除指定的key
-- 示例命令：
-- eval "local keys = redis.call('keys', ARGV[1]) for i=1,#keys,5000 do redis.call('del', unpack(keys, i, math.min(i+4999, #keys))) end return #keys" 0 'inventory-center:inventory*'
-- 脚本如下：
-- ARGV[1] 'inventory-center:inventory*'
local keys = redis.call('keys', ARGV[1])
for i=1,#keys,5000 do redis.call('del', unpack(keys, i, math.min(i+4999, #keys)))
end return #keys