package com.rsmnarts.valoo.infrastructure.configuration;

import java.time.Duration;

import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class CacheConfig {

	@Bean
	public RedisCacheConfiguration cacheConfiguration() {
		return RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(Duration.ofMinutes(60))
				.disableCachingNullValues()
				.serializeValuesWith(SerializationPair
						.fromSerializer(RedisSerializer.json()));
	}

	@Bean
	public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
		return (builder) -> builder
				.withCacheConfiguration("valorant-version",
						RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(24)))
				.withCacheConfiguration("weapon-skin-details",
						RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofDays(30)));
	}
}
