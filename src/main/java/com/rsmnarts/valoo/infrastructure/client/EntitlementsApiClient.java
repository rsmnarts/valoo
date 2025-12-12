package com.rsmnarts.valoo.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.rsmnarts.valoo.infrastructure.client.dto.RiotAuthDto;

@FeignClient(name = "riot-entitlements-api", url = "https://entitlements.auth.riotgames.com")
public interface EntitlementsApiClient {

	@PostMapping(value = "/api/token/v1", consumes = "application/json")
	RiotAuthDto.EntitlementsResponse getEntitlements(@RequestHeader("Authorization") String authorization,
			@RequestBody String body);
}
