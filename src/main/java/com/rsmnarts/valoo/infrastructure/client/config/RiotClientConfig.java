package com.rsmnarts.valoo.infrastructure.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.codec.ErrorDecoder;

@Configuration
public class RiotClientConfig {

	@Bean
	public ErrorDecoder errorDecoder() {
		return new RiotValorantApiErrorDecoder();
	}
}
