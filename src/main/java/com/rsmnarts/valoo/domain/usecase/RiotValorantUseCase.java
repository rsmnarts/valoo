package com.rsmnarts.valoo.domain.usecase;

import java.util.List;

import com.rsmnarts.valoo.domain.model.DailyStore;
import com.rsmnarts.valoo.domain.model.MatchHistory;
import com.rsmnarts.valoo.domain.model.NightMarket;
import com.rsmnarts.valoo.infrastructure.client.dto.CompetitiveUpdatesResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.PlayerNameResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.StorefrontResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.WalletResponse;

public interface RiotValorantUseCase {
	StorefrontResponse getStorefront(String puuid, String accessToken, String entitlementsToken, String region);

	DailyStore getDailyStores(String puuid, String accessToken, String entitlementsToken, String region);

	List<PlayerNameResponse> getPlayerName(String puuid, String accessToken, String entitlementsToken, String region);

	NightMarket getNightMarket(String puuid, String accessToken, String entitlementsToken, String region);

	MatchHistory getMatchHistory(String puuid, int startIndex, int endIndex, String accessToken, String entitlementsToken,
			String region);

	WalletResponse getWallet(String puuid, String accessToken, String entitlementsToken, String region);

	CompetitiveUpdatesResponse getCompetitiveUpdates(String puuid, String accessToken, String entitlementsToken,
			String region);

	PlayerNameResponse getPlayer(String puuid, String accessToken, String entitlementsToken, String region);

	void forceRefreshMatchHistory(String puuid, String accessToken, String entitlementsToken, String region);
}
