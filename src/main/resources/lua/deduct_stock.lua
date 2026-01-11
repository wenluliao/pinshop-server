-- Flash Sale Stock Deduction Lua Script
-- Atomic stock check and deduction with user limit validation
--
-- KEYS[1]: flash:stock:{skuId}
-- KEYS[2]: flash:user:{eventId}:{skuId}
-- ARGV[1]: userId
-- ARGV[2]: count (optional, default 1)

local stock_key = KEYS[1]
local user_key = KEYS[2]
local user_id = ARGV[1]
local count = tonumber(ARGV[2] or 1)

-- Check if user already bought
if redis.call('sismember', user_key, user_id) == 1 then
    return -2  -- User already bought
end

-- Get current stock
local current_stock = tonumber(redis.call('get', stock_key) or '0')

-- Check if stock is sufficient
if current_stock < count then
    return -1  -- Insufficient stock
end

-- Deduct stock
redis.call('decrby', stock_key, count)

-- Add user to bought set
redis.call('sadd', user_key, user_id)

-- Return remaining stock
return current_stock - count
