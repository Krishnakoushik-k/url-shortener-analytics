package com.portfolio.urlshortener.config;
import org.springframework.context.annotation.Bean; 
 import org.springframework.context.annotation.Configuration;
 import org.springframework.data.redis.connection.RedisConnectionFactory;
 import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
@Configuration public class RedisConfig {
 @Bean StringRedisTemplate stringRedisTemplate(RedisConnectionFactory f){return new StringRedisTemplate(f);}
 @Bean DefaultRedisScript<Long> rateLimitScript(){var s=new DefaultRedisScript<Long>();s.setScriptText("local n=redis.call('INCR',KEYS[1]); if n==1 then redis.call('EXPIRE',KEYS[1],ARGV[1]); end; return n");s.setResultType(Long.class);return s;}
}
