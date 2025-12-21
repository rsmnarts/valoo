package com.rsmnarts.valoo.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rsmnarts.valoo.domain.model.DailyStore;
import com.rsmnarts.valoo.domain.model.MatchHistory;
import com.rsmnarts.valoo.domain.model.NightMarket;
import com.rsmnarts.valoo.domain.usecase.RiotValorantUseCase;
import com.rsmnarts.valoo.infrastructure.client.dto.CompetitiveUpdatesResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.PlayerNameResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.StorefrontResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.WalletResponse;

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
				.ok(riotValoUseCase.getStorefront(puuid, cleanToken(accessToken), entitlementsToken, region));
	}

	@GetMapping("/storefront/{puuid}/daily-store")
	public ResponseEntity<DailyStore> getDailyStores(@PathVariable String puuid,
			@RequestHeader("Authorization") String accessToken,
			@RequestHeader("Entitlements-Token") String entitlementsToken,
			@RequestHeader(value = "Region", defaultValue = "ap") String region) {
		return ResponseEntity.ok(riotValoUseCase
				.getDailyStores(puuid, cleanToken(accessToken), entitlementsToken, region));
	}

	@GetMapping("/storefront/{puuid}/night-market")
	public ResponseEntity<NightMarket> getNightMarket(@PathVariable String puuid,
			@RequestHeader("Authorization") String accessToken,
			@RequestHeader("Entitlements-Token") String entitlementsToken,
			@RequestHeader(value = "Region", defaultValue = "ap") String region) {
		return ResponseEntity.ok(riotValoUseCase
				.getNightMarket(puuid, cleanToken(accessToken), entitlementsToken, region));
	}

	@GetMapping("/storefront/{puuid}/match-history")
	public ResponseEntity<MatchHistory> getMatchHistory(@PathVariable String puuid,
			@RequestParam(value = "startIndex", defaultValue = "0") int startIndex,
			@RequestParam(value = "endIndex", defaultValue = "5") int endIndex,
			@RequestHeader("Authorization") String accessToken,
			@RequestHeader("Entitlements-Token") String entitlementsToken,
			@RequestHeader(value = "Region", defaultValue = "ap") String region) {
		return ResponseEntity.ok(riotValoUseCase
				.getMatchHistory(puuid, startIndex, endIndex, cleanToken(accessToken), entitlementsToken,
						region));
	}

	@PostMapping("/match-history/{puuid}/refresh")
	public ResponseEntity<Void> forceRefreshMatchHistory(@PathVariable String puuid,
			@RequestHeader("Authorization") String accessToken,
			@RequestHeader("Entitlements-Token") String entitlementsToken,
			@RequestHeader(value = "Region", defaultValue = "ap") String region) {
		riotValoUseCase.forceRefreshMatchHistory(puuid, cleanToken(accessToken), entitlementsToken, region);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/players/{puuid}")
	public ResponseEntity<List<PlayerNameResponse>> getPlayerName(
			@PathVariable String puuid,
			@RequestHeader("Authorization") String accessToken,
			@RequestHeader("Entitlements-Token") String entitlementsToken,
			@RequestHeader(value = "Region", defaultValue = "ap") String region) {
		return ResponseEntity
				.ok(riotValoUseCase.getPlayerName(puuid, cleanToken(accessToken), entitlementsToken, region));
	}

	@GetMapping("/wallet/{puuid}")
	public ResponseEntity<WalletResponse> getWallet(@PathVariable String puuid,
			@RequestHeader("Authorization") String accessToken,
			@RequestHeader("Entitlements-Token") String entitlementsToken,
			@RequestHeader(value = "Region", defaultValue = "ap") String region) {
		return ResponseEntity
				.ok(riotValoUseCase.getWallet(puuid, cleanToken(accessToken), entitlementsToken, region));
	}

	@GetMapping("/competitive-updates/{puuid}")
	public ResponseEntity<CompetitiveUpdatesResponse> getCompetitiveUpdates(@PathVariable String puuid,
			@RequestHeader("Authorization") String accessToken,
			@RequestHeader("Entitlements-Token") String entitlementsToken,
			@RequestHeader(value = "Region", defaultValue = "ap") String region) {
		return ResponseEntity.ok(riotValoUseCase.getCompetitiveUpdates(puuid, cleanToken(accessToken),
				entitlementsToken, region));
	}

	@GetMapping("/player/{puuid}")
	public ResponseEntity<PlayerNameResponse> getPlayer(@PathVariable String puuid,
			@RequestHeader("Authorization") String accessToken,
			@RequestHeader("Entitlements-Token") String entitlementsToken,
			@RequestHeader(value = "Region", defaultValue = "ap") String region) {
		return ResponseEntity
				.ok(riotValoUseCase.getPlayer(puuid, cleanToken(accessToken), entitlementsToken, region));
	}

	private String cleanToken(String token) {
		return token != null ? token.replace("Bearer ", "").trim() : "";
	}
}
