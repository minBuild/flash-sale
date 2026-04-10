local stockKey = KEYS[1]
local usersKey = KEYS[2]
local userId = ARGV[1]

if redis.call('SISMEMBER', usersKey, userId) == 1 then
    return 3 -- Fail: DUPLICATED
end

local currentStock = tonumber(redis.call('GET', stockKey))
if currentStock == nil or currentStock <= 0 then
    return 2 -- Fail: SOLD_OUT
end

redis.call('DECR', stockKey)
redis.call('SADD', usersKey, userId)

return 1 -- SUCCESS
