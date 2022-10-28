package cn.zhangyou705.redis.lock;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZhangYou
 * @description lua
 * @date 2022/10/28
 */
public class RedisLockLua {

    public static boolean lock(RedisTemplate<String, Object> redisTemplate, String key, String id, long expire) {
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setScriptText("if (redis.call('exists', KEYS[1]) == 0) then redis.call('hset', KEYS[1],ARGV[1], 1); " +
                "redis.call('pexpire', KEYS[1], ARGV[2]); return nil; end; " +
                "if (redis.call('hexists', KEYS[1], ARGV[1]) == 1) then redis.call('hincrby', KEYS[1], ARGV[1], 1); " +
                "redis.call('pexpire', KEYS[1], ARGV[2]); return nil; end; return redis.call('pttl', KEYS[1]);");
        script.setResultType(String.class);
        List<String> list = new ArrayList<>();
        list.add(key);
        String result = redisTemplate.execute(script, list, id, expire);
        return result == null;
    }

    public static void unlock(RedisTemplate<String, Object> redisTemplate, String key, String id) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText("if (redis.call('exists', KEYS[1]) == 0) then return 0; end; " +
                "if (redis.call('hexists', KEYS[1], ARGV[1]) == 0) then return 0; end; " +
                "local counter = redis.call('hincrby', KEYS[1], ARGV[1], -1); " +
                "if (counter > 0) then return 1; " +
                "else redis.call('del', KEYS[1]); return 1; end;");
        script.setResultType(Long.class);
        List<String> list = new ArrayList<>();
        list.add(key);
        redisTemplate.execute(script, list, id);
    }

    public static boolean isLocked(RedisTemplate<String, Object> redisTemplate, String key, String id) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText("if (redis.call('hexists', KEYS[1], ARGV[1]) == 0) then return 0; end; " +
                "return 1;");
        script.setResultType(Long.class);
        List<String> list = new ArrayList<>();
        list.add(key);
        Long result = redisTemplate.execute(script, list, id);
        return result != null && result == 1L;
    }

}
