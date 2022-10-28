package cn.zhangyou705.redis.exception;

/**
 * @author ZhangYou
 * @description
 * @date 2022/10/28
 */
public class RedisLockException extends RuntimeException {

    public RedisLockException(String message) {
        super(message);
    }

    public RedisLockException(String message, Throwable cause) {
        super(message, cause);
    }

}
