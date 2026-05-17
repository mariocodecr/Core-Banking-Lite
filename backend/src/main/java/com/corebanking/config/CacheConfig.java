package com.corebanking.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_DASHBOARD_SUMMARY = "dashboardSummary";
    public static final String CACHE_CUSTOMERS         = "customers";
    public static final String CACHE_EXCHANGE_RATES    = "exchangeRates";
    public static final String CACHE_MARKET_DATA       = "marketData";

    @Bean
    public RedisCacheConfiguration defaultCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()));
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return builder -> builder
                .withCacheConfiguration(CACHE_DASHBOARD_SUMMARY,
                        defaultCacheConfiguration().entryTtl(Duration.ofMinutes(1)))
                .withCacheConfiguration(CACHE_CUSTOMERS,
                        defaultCacheConfiguration().entryTtl(Duration.ofMinutes(10)))
                .withCacheConfiguration(CACHE_EXCHANGE_RATES,
                        defaultCacheConfiguration().entryTtl(Duration.ofHours(4)))
                .withCacheConfiguration(CACHE_MARKET_DATA,
                        defaultCacheConfiguration().entryTtl(Duration.ofHours(1)));
    }
}
