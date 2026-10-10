-- Redis Token Bucket Rate Limiter Lua Script
-- KEYS[1]: rate limit key (e.g. ratelimit:ip:127.0.0.1)
-- ARGV[1]: bucket capacity (max tokens)
-- ARGV[2]: refill rate per millisecond
-- ARGV[3]: requested tokens (typically 1)
-- ARGV[4]: current timestamp in milliseconds
-- ARGV[5]: TTL in seconds for the key

local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refill_rate_per_ms = tonumber(ARGV[2])
local requested = tonumber(ARGV[3])
local now = tonumber(ARGV[4])
local ttl_seconds = tonumber(ARGV[5])

-- Fetch current state from hash
local data = redis.call('HMGET', key, 'tokens', 'last_refill')
local current_tokens = tonumber(data[1])
local last_refill = tonumber(data[2])

if current_tokens == nil then
    current_tokens = capacity
    last_refill = now
else
    local elapsed = math.max(0, now - last_refill)
    local refilled = elapsed * refill_rate_per_ms
    current_tokens = math.min(capacity, current_tokens + refilled)
    last_refill = now
end

local allowed = 0
local remaining = current_tokens
local retry_after_seconds = 0

if current_tokens >= requested then
    allowed = 1
    current_tokens = current_tokens - requested
    remaining = current_tokens
else
    allowed = 0
    local needed = requested - current_tokens
    if refill_rate_per_ms > 0 then
        retry_after_seconds = math.ceil((needed / refill_rate_per_ms) / 1000)
    else
        retry_after_seconds = 60
    end
    if retry_after_seconds < 1 then
        retry_after_seconds = 1
    end
end

-- Save updated state
redis.call('HMSET', key, 'tokens', current_tokens, 'last_refill', last_refill)
redis.call('EXPIRE', key, ttl_seconds)

-- Return array: [allowed (1 or 0), remaining_tokens, retry_after_seconds, capacity]
return { allowed, math.floor(remaining), retry_after_seconds, capacity }
