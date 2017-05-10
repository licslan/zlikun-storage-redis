-- 下面一段代码用于实现指定时间内低于指定最大访问次数返回1，否则返回0

-- Lua方法中，数组索引从1开始计
local times = redis.call('incr' ,KEYS[1])

if times == 1 then
	-- KEYS[1]键刚创建，为其设置缓存过期时间
	redis.call('expire' ,KEYS[1] ,ARGV[1])
end

if times > tonumber(ARGV[2]) then
	return 0
end

return 1