# Redisson 信号量使用方法
```java
RedissonClient client = null;
RPermitExpirableSemaphore semaphore = client.getPermitExpirableSemaphore(semaphoreName);
semaphore.trySetPermits((int) limit);

//每申请一次信号量，expire信号量的生命SEMAPHORE_LIFE_EXPIRE秒
semaphore.expire(5*60, TimeUnit.SECONDS);

//尝试次数init
int time = 0;
int MAX_TRY_ACQUIRE_TIME = 5;
while (MAX_TRY_ACQUIRE_TIME > time) {
    //尝试获取信号量
    String permitId = semaphore.tryAcquire(waitTime, leaseTime, TimeUnit.MILLISECONDS);
    //获取信号量失败
    if (null == permitId) {
        time++;
        continue;
    }
    //获取信号量成功,设置acquireId和permitId的映射关系
    if (!RedisUtils.hset(getMapName(semaphoreName), acquireId, permitId, getSemaphoreAcquireExpire(), TimeUnit.MILLISECONDS)) {
        //如果失败，释放资源
        semaphore.release(permitId);
        return acquireError();
    }
    return 0;
}
```
## RPermitExpirableSemaphore 实现原理
```java
public RFuture<String> tryAcquireAsync(int permits, long timeoutDate) {
    if (permits < 0) {
        throw new IllegalArgumentException("Permits amount can't be negative");
    }

    String id = generateId();
    return commandExecutor.evalWriteAsync(getName(), LongCodec.INSTANCE, RedisCommands.EVAL_STRING_DATA, 
                "local expiredIds = redis.call('zrangebyscore', KEYS[2], 0, ARGV[4], 'limit', 0, ARGV[1]); " +
                "if #expiredIds > 0 then " +
                    "redis.call('zrem', KEYS[2], unpack(expiredIds)); " +
                    "local value = redis.call('incrby', KEYS[1], #expiredIds); " + 
                    "if tonumber(value) > 0 then " +
                        "redis.call('publish', KEYS[3], value); " +
                    "end;" +
                "end; " +
                "local value = redis.call('get', KEYS[1]); " +
                "if (value ~= false and tonumber(value) >= tonumber(ARGV[1])) then " +
                    "redis.call('decrby', KEYS[1], ARGV[1]); " +
                    "redis.call('zadd', KEYS[2], ARGV[2], ARGV[3]); " +
                    "return ARGV[3]; " +
                "end; " +
                "local v = redis.call('zrange', KEYS[2], 0, 0, 'WITHSCORES'); " + 
                "if v[1] ~= nil and v[2] ~= ARGV[5] then " + 
                    "return ':' .. tostring(v[2]); " + 
                "end " +
                "return nil;",
                Arrays.<Object>asList(getName(), timeoutName, getChannelName()), permits, timeoutDate, id, System.currentTimeMillis(), nonExpirableTimeout);
}
```