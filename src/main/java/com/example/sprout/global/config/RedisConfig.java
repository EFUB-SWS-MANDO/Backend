package com.example.sprout.global.config;

import com.example.sprout.domain.category.dto.CategoryDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, String> redisTemplate (RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();

        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        return template;
    }

    @Bean
    public RedisCacheManager redisCacheManager (RedisConnectionFactory connectionFactory) {

        JsonMapper objectMapper = JsonMapper.builder().build();

        JacksonJsonRedisSerializer<CategoryDto> categoriesSerializer =
                new JacksonJsonRedisSerializer<>(objectMapper, CategoryDto.class);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config.serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                GenericJacksonJsonRedisSerializer.builder().build()
                        )
                ))
                .withCacheConfiguration(
                        "categories",
                        config.entryTtl(Duration.ofDays(7))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(categoriesSerializer))
                )
                .build();
    }
}
