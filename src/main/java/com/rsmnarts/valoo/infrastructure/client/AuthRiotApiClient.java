package com.rsmnarts.valoo.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.rsmnarts.valoo.infrastructure.client.dto.RiotAuthDto;

import feign.Response;

@FeignClient(name = "riot-auth-api", url = "https://auth.riotgames.com")
public interface AuthRiotApiClient {

	@PostMapping("/api/v1/authorization")
	Response authorize(@RequestBody RiotAuthDto.AuthRequest request, @RequestHeader("User-Agent") String userAgent);

	@PutMapping("/api/v1/authorization")
	ResponseEntity<RiotAuthDto.AuthResponse> login(@RequestBody RiotAuthDto.LoginRequest request,
			@RequestHeader("Cookie") String cookie,
			@RequestHeader("User-Agent") String userAgent);

	@PostMapping("/userinfo")
	RiotAuthDto.UserInfoResponse getUserInfo(@RequestHeader("Authorization") String authorization);
}
