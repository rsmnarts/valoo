package com.rsmnarts.valoo.infrastructure.client;

import java.net.URI;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.rsmnarts.valoo.infrastructure.client.config.RiotClientConfig;
import com.rsmnarts.valoo.infrastructure.client.dto.MatchDetailsResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.MatchHistoryResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.StorefrontResponse;

import feign.Headers;

@FeignClient(name = "riot-store-api", url = "https://pd.ap.a.pvp.net", configuration = RiotClientConfig.class)
public interface RiotValorantApiClient {

	@PostMapping("/store/v3/storefront/{puuid}")
	StorefrontResponse getStorefront(
			URI baseUrl,
			@PathVariable("puuid") String puuid,
			@RequestBody String body,
			@RequestHeader("X-Riot-ClientPlatform") String clientPlatform,
			@RequestHeader("X-Riot-ClientVersion") String clientVersion,
			@RequestHeader("X-Riot-Entitlements-JWT") String entitlementsToken,
			@RequestHeader("Authorization") String authorization);

	@PutMapping("/name-service/v2/players")
	@Headers("Content-Type: application/json")
	String getPlayerName(
			URI baseUrl,
			@RequestBody Object body,
			@RequestHeader("X-Riot-ClientPlatform") String clientPlatform,
			@RequestHeader("X-Riot-ClientVersion") String clientVersion,
			@RequestHeader("X-Riot-Entitlements-JWT") String entitlementsToken,
			@RequestHeader("Authorization") String authorization);

	@GetMapping("/match-history/v1/history/{puuid}")
	MatchHistoryResponse getMatchHistory(
			URI baseUrl,
			@PathVariable("puuid") String puuid,
			@RequestParam("startIndex") int startIndex,
			@RequestParam("endIndex") int endIndex,
			@RequestHeader("X-Riot-ClientPlatform") String clientPlatform,
			@RequestHeader("X-Riot-ClientVersion") String clientVersion,
			@RequestHeader("X-Riot-Entitlements-JWT") String entitlementsToken,
			@RequestHeader("Authorization") String authorization);

	@GetMapping("/match-details/v1/matches/{matchId}")
	MatchDetailsResponse getMatchDetails(
			URI baseUrl,
			@PathVariable("matchId") String matchId,
			@RequestHeader("X-Riot-ClientPlatform") String clientPlatform,
			@RequestHeader("X-Riot-ClientVersion") String clientVersion,
			@RequestHeader("X-Riot-Entitlements-JWT") String entitlementsToken,
			@RequestHeader("Authorization") String authorization);
}
