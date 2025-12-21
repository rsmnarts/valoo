package com.rsmnarts.valoo.infrastructure.service;

import java.net.URI;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.rsmnarts.valoo.common.utils.json;
import com.rsmnarts.valoo.infrastructure.client.RiotValorantApiClient;
import com.rsmnarts.valoo.infrastructure.client.dto.CompetitiveUpdatesResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.MatchDetailsResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiotMatchDataService {

	private final RiotValorantApiClient storeApiClient;

	@Cacheable(value = "match-details", key = "#matchId", unless = "#result == null")
	public MatchDetailsResponse getMatchDetails(URI uri, String matchId, String clientPlatform, String clientVersion,
			String entitlementsToken, String authorization) {
		log.debug("Fetching match details for match {}", matchId);
		return storeApiClient.getMatchDetails(uri, matchId, clientPlatform, clientVersion, entitlementsToken,
				authorization);
	}

	@Cacheable(value = "competitive-updates-map", key = "#puuid + '-' + #matchId", unless = "#result == null")
	public Integer getRankedRatingEarned(URI uri, String puuid, String matchId, String clientPlatform,
			String clientVersion,
			String entitlementsToken, String authorization) {
		log.debug("Fetching competitive updates for player {} in match {}", puuid, matchId);
		try {
			String resp = storeApiClient.getCompetitiveUpdates(uri, puuid, 0, 15, "competitive",
					clientPlatform, clientVersion, entitlementsToken, authorization);
			CompetitiveUpdatesResponse updates = json.Unmarshal(resp, CompetitiveUpdatesResponse.class);
			if (updates != null && updates.getMatches() != null) {
				return updates.getMatches().stream()
						.filter(m -> m.getMatchId().equalsIgnoreCase(matchId))
						.map(CompetitiveUpdatesResponse.CompetitiveUpdate::getRankedRatingEarned)
						.findFirst().orElse(null);
			}
		} catch (Exception e) {
			log.error("Error fetching competitive updates for {}: {}", puuid, e.getMessage());
		}
		return null;
	}
}
