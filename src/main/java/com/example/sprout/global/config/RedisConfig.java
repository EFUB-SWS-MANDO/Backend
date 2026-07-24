package com.example.sprout.global.config;

import com.example.sprout.domain.category.dto.CategoryDto;
import com.example.sprout.domain.interview.dto.response.InterviewSessionCursorResponse;
import com.example.sprout.domain.template.dto.TemplateDto;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

@EnableCaching
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

        JacksonJsonRedisSerializer<TemplateDto> templateSerializer =
                new JacksonJsonRedisSerializer<>(objectMapper, TemplateDto.class);

        JacksonJsonRedisSerializer<CategoryDto> categoriesSerializer =
                new JacksonJsonRedisSerializer<>(objectMapper, CategoryDto.class);

        JacksonJsonRedisSerializer<InterviewSessionCursorResponse> interviewSessionCursorSerializer =
                new JacksonJsonRedisSerializer<>(objectMapper, InterviewSessionCursorResponse.class);


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
                        "templates",
                        config.entryTtl(Duration.ofDays(7))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(templateSerializer))
                )
                .withCacheConfiguration(
                        "categories",
                        config.entryTtl(Duration.ofDays(7))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(categoriesSerializer))
                )
                .withCacheConfiguration(
                        "interviewSessions",
                        config.entryTtl(Duration.ofMinutes(30))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(interviewSessionCursorSerializer))
                )
                .build();
    }
}
