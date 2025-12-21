package com.rsmnarts.valoo.infrastructure.configuration;

import java.time.Duration;

import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
@EnableCaching
public class CacheConfig {

	@Bean
	public RedisCacheConfiguration cacheConfiguration() {
		return createCacheConfig(Duration.ofMinutes(5));
	}

	@Bean
	public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
		return (builder) -> builder
				.withCacheConfiguration("valorant-version", createCacheConfig(Duration.ofHours(24)))
				.withCacheConfiguration("weapon-skin-details", createCacheConfig(Duration.ofDays(30)))
				.withCacheConfiguration("match-details", createCacheConfig(Duration.ofDays(7)))
				.withCacheConfiguration("competitive-updates-map", createCacheConfig(Duration.ofDays(7)))
				.withCacheConfiguration("match-history-list", createCacheConfig(Duration.ofDays(1)));
	}

	private RedisCacheConfiguration createCacheConfig(Duration ttl) {
		return RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(ttl)
				.disableCachingNullValues()
				.serializeValuesWith(SerializationPair.fromSerializer(RedisSerializer.json()));
	}
}
