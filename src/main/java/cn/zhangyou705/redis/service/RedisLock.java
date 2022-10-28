package cn.zhangyou705.redis.service;

import cn.zhangyou705.redis.exception.RedisLockException;

import java.util.concurrent.TimeUnit;

/**
 * @author ZhangYou
 * @description
 * @date 2022/10/28
 */
public interface RedisLock {

    void lock(long expire, TimeUnit timeUnit) throws InterruptedException, RedisLockException;

    boolean tryLock(long expire, TimeUnit timeUnit) throws RedisLockException;

    boolean tryLock(long timeout, long expire, TimeUnit timeUnit) throws InterruptedException, RedisLockException;

    boolean isHeldByCurrentThread();

    boolean isLocked() throws RedisLockException;

    void unlock() throws RedisLockException;
}
