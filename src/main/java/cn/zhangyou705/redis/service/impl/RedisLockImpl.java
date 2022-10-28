package cn.zhangyou705.redis.service.impl;

import cn.zhangyou705.redis.lock.RedisLockLua;
import cn.zhangyou705.redis.service.RedisLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * @author ZhangYou
 * @description
 * @date 2022/10/28
 */
public class RedisLockImpl implements RedisLock {
    private static final Logger logger = LoggerFactory.getLogger(RedisLock.class);
    private Thread thread;
    private final String key;
    private final int unlockRetry;
    private final RedisTemplate<String, Object> redisTemplate;

    public static RedisLock create(String key, RedisTemplate<String, Object> redisTemplate) {
        return create(key, redisTemplate, 1);
    }

    public static RedisLockImpl create(String key, RedisTemplate<String, Object> redisTemplate, int unlockRetry) {
        return new RedisLockImpl(key, redisTemplate, unlockRetry);
    }

    private RedisLockImpl(String key, RedisTemplate<String, Object> redisTemplate, int unlockRetry) {
        this.key = key;
        this.redisTemplate = redisTemplate;
        this.unlockRetry = unlockRetry;
    }

    @Override
    public void lock(long expire, TimeUnit timeUnit) {
        if (expire <= 0L) throw new IllegalArgumentException("expire time least gt zero");
        String field = getLockName(Thread.currentThread().getId());
        boolean result;
        for (; ; ) {
            result = RedisLockLua.lock(redisTemplate, key, field, timeUnit.toMillis(expire));
            if (result) {
                thread = Thread.currentThread();
                return;
            }
        }
    }

    @Override
    public boolean tryLock(long expire, TimeUnit timeUnit) {
        String field = getLockName(Thread.currentThread().getId());
        boolean result = RedisLockLua.lock(redisTemplate, key, field, timeUnit.toMillis(expire));
        if (result) {
            thread = Thread.currentThread();
            return true;
        }
        return false;
    }

    @Override
    public boolean tryLock(long timeout, long expire, TimeUnit timeUnit) {
        if (expire <= 0L) throw new IllegalArgumentException("expire time least gt zero");
        if (timeout <= 0L) throw new IllegalArgumentException("timeout time least gt zero");
        final long deadline = System.nanoTime() + timeUnit.toNanos(timeout);
        String field = getLockName(Thread.currentThread().getId());
        boolean result;
        for (; ; ) {
            result = RedisLockLua.lock(redisTemplate, key, field, timeUnit.toMillis(expire));
            if (result) {
                thread = Thread.currentThread();
                return true;
            } else {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0L)
                    return false;
                LockSupport.parkNanos(remaining);
            }
        }
    }

    @Override
    public boolean isHeldByCurrentThread() {
        return thread == Thread.currentThread();
    }

    @Override
    public boolean isLocked() {
        return RedisLockLua.isLocked(redisTemplate, key, getLockName(Thread.currentThread().getId()));
    }

    @Override
    public void unlock() {
        if (thread != Thread.currentThread()) throw new IllegalMonitorStateException();
        String field = getLockName(Thread.currentThread().getId());
        for (int i = 0; i <= unlockRetry; i++) {
            try {
                RedisLockLua.unlock(redisTemplate, key, field);
                break;
            } catch (Exception e) {
                logger.error("当前线程解锁异常,线程ID:{},error:{}", Thread.currentThread().getId(), e.getMessage());
            }
            if (unlockRetry == i) logger.warn("当前线程解锁异常,线程ID:{}", Thread.currentThread().getId());
        }
    }

    String getLockName(long threadId) {
        return UUID.randomUUID() + ":" + threadId;
    }

}
