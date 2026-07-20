package com.example.sprout.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;


@ActiveProfiles("test")
@SpringBootTest
@Testcontainers
public abstract class IntegrationTestSupport {

    @ServiceConnection
    static final PostgreSQLContainer postgres;

    @ServiceConnection
    static final GenericContainer redis;

    static {
        postgres = new PostgreSQLContainer("postgres:17")
                .withDatabaseName("sprout_test")
                .withUsername("test")
                .withPassword("test");
        postgres.start();

        redis = new GenericContainer("redis:7")
                .withExposedPorts(6379);
        redis.start();
    }

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void clearRedis() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }
}
