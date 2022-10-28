package cn.zhangyou705.redis;

import cn.zhangyou705.redis.service.RedisLock;
import cn.zhangyou705.redis.service.impl.RedisLockImpl;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.concurrent.TimeUnit;

/**
 * @author ZhangYou
 * @description 测试
 * @date 2022/10/28
 */
public class LockTest {
    RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_HOST = "";
    private static final int REDIS_PORT = 6379;
    private static final int REDIS_DB = 1;
    private static final String REDIS_PWD = "";

    @Before
    public void redisConfig() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(REDIS_HOST);
        redisStandaloneConfiguration.setPort(REDIS_PORT);
        redisStandaloneConfiguration.setDatabase(REDIS_DB);
        RedisPassword redisPassword = RedisPassword.of(REDIS_PWD);
        redisStandaloneConfiguration.setPassword(redisPassword);
        RedisConnectionFactory factory = new JedisConnectionFactory(redisStandaloneConfiguration);

        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
    }

    @Test
    public void lock() {
        RedisLock redisLock = RedisLockImpl.create("test", redisTemplate);
        try {

            boolean result = redisLock.tryLock(600, TimeUnit.SECONDS);
            System.out.println("加锁结果1：" + result);
            result = redisLock.tryLock(600, TimeUnit.SECONDS);
            System.out.println("加锁结果2：" + result);
            result = redisLock.tryLock(600, TimeUnit.SECONDS);
            System.out.println("加锁结果3：" + result);
            redisLock.unlock();
            result = redisLock.tryLock(600, TimeUnit.SECONDS);
            System.out.println("加锁结果4：" + result);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
