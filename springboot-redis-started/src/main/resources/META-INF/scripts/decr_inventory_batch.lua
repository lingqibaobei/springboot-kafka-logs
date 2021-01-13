--
-- Created by IntelliJ IDEA.
-- User: fuhw/Dean
-- Date: 2019-05-24
-- Time: 17:32
-- To change this template use File | Settings | File Templates.

-- @desc 批量扣减库存Lua脚本
-- 库存（stock）-1：表示不限库存
-- 库存（stock）0：表示没有库存
-- 库存（stock）大于0：表示剩余库存
--
-- @params 库存key
-- @return
-- 		-3:库存未初始化
-- 		-2:库存不足
-- 		-1:不限库存
-- 	    redis缓存的库存(value)是-1表示不限库存，直接返回-1
-- 	  {
-- 	    {InitInventory}:SET:INVENTORY:MAP:SPU_SKU_IDS:{spuId}:{skuId},  sku 光了
--      {InitInventory}:SET:INVENTORY:MAP:LIVE_SPU_IDS:{liveId},        活动光了
--      {InitInventory}:SET:INVENTORY:MAP:SPU_SKU_IDS:{spuId}           商品光了

for i, v in ipairs(KEYS) do
    local nFindStartIndex = 1;
    local nSplitIndex = 1;
    local nSplitArray = {};
    while true do
        local nFindLastIndex = string.find(KEYS[i], '-', nFindStartIndex);
        if not nFindLastIndex then
            nSplitArray[nSplitIndex] = string.sub(KEYS[i], nFindStartIndex, string.len(KEYS[i]));
            break;
        end;
        nSplitArray[nSplitIndex] = string.sub(KEYS[i], nFindStartIndex, nFindLastIndex - 1);
        nFindStartIndex = nFindLastIndex + string.len('-');
        nSplitIndex = nSplitIndex + 1;
    end;
    if (redis.call('exists', nSplitArray[1]) == 1) then
        local stock = tonumber(redis.call('hget', nSplitArray[1], 'skuCurrentStock'));
        local num = tonumber(ARGV[i]);
        if (stock == -1) then
            return -1;
        end;
        if (stock < num) then
            return -2;
        end;
    else
        return -3;
    end;
end;
--开始处理减库存
local result = {}
for i, v in ipairs(KEYS) do
    --处理字符串key，进行截取，为后面获取liveID，spuid，skuID做作准备。
    local nFindStartIndex = 1;
    local nSplitIndex = 1;
    local nSplitArray = {};
    while true do
        local nFindLastIndex = string.find(KEYS[i], '-', nFindStartIndex);
        if not nFindLastIndex then
            nSplitArray[nSplitIndex] = string.sub(KEYS[i], nFindStartIndex, string.len(KEYS[i]));
            break;
        end;
        nSplitArray[nSplitIndex] = string.sub(KEYS[i], nFindStartIndex, nFindLastIndex - 1);
        nFindStartIndex = nFindLastIndex + string.len('-');
        nSplitIndex = nSplitIndex + 1;
    end;
    --设置修改时间
    redis.call('hset', nSplitArray[1], 'modifyTime', ARGV[table.getn(KEYS) + 1])
    --处理活动和spu的key
    --key[] 传入规则：{InitInventory}:{liveid}:{skuid}-{liveid}:{spuid}
    local live_id = string.sub(nSplitArray[2], 1, string.find(nSplitArray[2], ':') - 1)
    local spu_id = string.sub(nSplitArray[2], string.find(nSplitArray[2], ':') + 1, -1)
    --反转获取skuId
    local ts = string.reverse(nSplitArray[1])
    local param1, param2 = string.find(ts, ':')
    local m = string.len(nSplitArray[1]) - param2 + 1
    local sku_id = string.sub(nSplitArray[1], m + 1, string.len(nSplitArray[1]))
    local live_spu_map_key = '{InitInventory}:SET:INVENTORY:MAP:LIVE_SPU_IDS:' .. live_id
    local spu_sku_map_key = '{InitInventory}:SET:INVENTORY:MAP:SPU_SKU_IDS:' .. spu_id
    --进行真正的库存扣减
    local num = redis.call('hincrby', nSplitArray[1], 'skuCurrentStock', 0 - tonumber(ARGV[i]));
    --如果刚好减为0，则处理减移除set中的skuid
    if (num == 0) then
        --sku光了
        table.insert(result, spu_sku_map_key .. ':' .. sku_id)
        if (redis.call('exists', spu_sku_map_key) == 1) then
            local sucess = redis.call('srem', spu_sku_map_key, sku_id);
            if (sucess == 1) then
                local num = redis.call('scard', spu_sku_map_key);
                if (num == 0) then
                    --商品光了
                    table.insert(result, spu_sku_map_key)
                    if (redis.call('exists', live_spu_map_key) == 1) then
                        local sucess = redis.call('srem', live_spu_map_key, spu_id);
                        if (sucess == 1) then
                            local num = redis.call('scard', live_spu_map_key);
                            if (num == 0) then
                                --活动光了
                                table.insert(result, live_spu_map_key)
                            end;
                        end;
                    end;
                end;
            end;
        end;
    end;
end;
return result;

