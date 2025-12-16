package com.rsmnarts.valoo.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rsmnarts.valoo.domain.model.DailyStore;
import com.rsmnarts.valoo.domain.model.MatchHistory;
import com.rsmnarts.valoo.domain.model.NightMarket;
import com.rsmnarts.valoo.domain.usecase.RiotValorantUseCase;
import com.rsmnarts.valoo.infrastructure.client.dto.PlayerNameResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.StorefrontResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/riot-valo")
@RequiredArgsConstructor
public class RiotValorantController {

	private final RiotValorantUseCase riotValoUseCase;

	@GetMapping("/storefront/{puuid}")
	public ResponseEntity<StorefrontResponse> getStorefront(@PathVariable String puuid,
			@RequestHeader("Authorization") String accessToken,
			@RequestHeader("Entitlements-Token") String entitlementsToken,
			@RequestHeader(value = "Region", defaultValue = "ap") String region) {
		return ResponseEntity
				.ok(riotValoUseCase.getStorefront(puuid, accessToken.replace("Bearer ", "").trim(), entitlementsToken, region));
	}

	@GetMapping("/storefront/{puuid}/daily-store")
	public ResponseEntity<DailyStore> getDailyStores(@PathVariable String puuid,
			@RequestHeader("Authorization") String accessToken,
			@RequestHeader("Entitlements-Token") String entitlementsToken,
			@RequestHeader(value = "Region", defaultValue = "ap") String region) {
		return ResponseEntity.ok(riotValoUseCase
				.getDailyStores(puuid, accessToken.replace("Bearer ", "").trim(), entitlementsToken, region));
	}

	@GetMapping("/storefront/{puuid}/night-market")
	public ResponseEntity<NightMarket> getNightMarket(@PathVariable String puuid,
			@RequestHeader("Authorization") String accessToken,
			@RequestHeader("Entitlements-Token") String entitlementsToken,
			@RequestHeader(value = "Region", defaultValue = "ap") String region) {
		return ResponseEntity.ok(riotValoUseCase
				.getNightMarket(puuid, accessToken.replace("Bearer ", "").trim(), entitlementsToken, region));
	}

	@GetMapping("/storefront/{puuid}/match-history")
	public ResponseEntity<MatchHistory> getMatchHistory(@PathVariable String puuid,
			@RequestHeader("Authorization") String accessToken,
			@RequestHeader("Entitlements-Token") String entitlementsToken,
			@RequestHeader(value = "Region", defaultValue = "ap") String region) {
		return ResponseEntity.ok(riotValoUseCase
				.getMatchHistory(puuid, accessToken.replace("Bearer ", "").trim(), entitlementsToken, region));
	}

	@GetMapping("/players/{puuid}")
	public ResponseEntity<List<PlayerNameResponse>> getPlayerName(
			@PathVariable String puuid,
			@RequestHeader("Authorization") String accessToken,
			@RequestHeader("Entitlements-Token") String entitlementsToken,
			@RequestHeader(value = "Region", defaultValue = "ap") String region) {
		return ResponseEntity
				.ok(riotValoUseCase.getPlayerName(puuid, accessToken.replace("Bearer ", "").trim(), entitlementsToken, region));
	}
}
