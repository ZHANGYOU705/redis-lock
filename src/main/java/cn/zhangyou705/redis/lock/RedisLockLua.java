package cn.zhangyou705.redis.lock;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.Collections;

/**
 * @author ZhangYou
 * @description lua
 * @date 2022/10/28
 */
public class RedisLockLua {
    public static boolean lock(RedisTemplate<String, Object> redisTemplate, String key, String id, long expire) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/redis_lock.lua")));
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(script, Collections.singletonList(key), id, expire);
        return result != null && result == 1;
    }

    public static void unlock(RedisTemplate<String, Object> redisTemplate, String key, String id) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/redis_unlock.lua")));
        script.setResultType(Long.class);

        redisTemplate.execute(script, Collections.singletonList(key), id);
    }

    public static boolean isLocked(RedisTemplate<String, Object> redisTemplate, String key, String id) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/redis_isLocked.lua")));
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(script, Collections.singletonList(key), id);
        return result != null && result == 1;
    }

}
