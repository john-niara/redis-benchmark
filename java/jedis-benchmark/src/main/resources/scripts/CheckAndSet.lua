if tonumber(redis.call("GET", KEYS[1])) < tonumber(ARGV[1]) then
    return redis.call("SET", KEYS[1], ARGV[1])
else
    return "NOT SET"
end
