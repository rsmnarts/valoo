package com.rsmnarts.valoo.infrastructure.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rsmnarts.valoo.common.utils.json;
import com.rsmnarts.valoo.domain.model.DailyStore;
import com.rsmnarts.valoo.domain.model.MatchHistory;
import com.rsmnarts.valoo.domain.model.NightMarket;
import com.rsmnarts.valoo.domain.model.RiotAuth;
import com.rsmnarts.valoo.domain.usecase.RiotValorantUseCase;
import com.rsmnarts.valoo.infrastructure.client.RiotValorantApiClient;
import com.rsmnarts.valoo.infrastructure.client.dto.AgentsResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.ClientPlatformRequest;
import com.rsmnarts.valoo.infrastructure.client.dto.CompetitiveTiersResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.ContentTierResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.CurrencyResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.DailyStoreLevelResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.MapsResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.MatchDetailsResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.MatchHistoryResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.PlayerNameResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.StorefrontResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.StorefrontResponse.SingleItemStoreOffer;
import com.rsmnarts.valoo.infrastructure.client.dto.VersionResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.WalletResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.WeaponSkinResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RiotValorantService implements RiotValorantUseCase {

	private final RiotValorantApiClient storeApiClient;
	private final ValorantMetadataService valorantMetadataService;

	public RiotValorantService(RiotValorantApiClient storeApiClient, ValorantMetadataService valorantMetadataService) {
		this.storeApiClient = storeApiClient;
		this.valorantMetadataService = valorantMetadataService;
	}

	private String determineShard(String region) {
		if (region == null) {
			return "ap"; // Default to AP if unknown
		}
		switch (region.toLowerCase()) {
			case "na":
			case "latam":
			case "br":
				return "na";
			case "pbe":
				return "pbe";
			case "eu":
				return "eu";
			case "ap":
				return "ap";
			case "kr":
				return "kr";
			default:
				return "ap";
		}
	}

	private URI getUrl(String shard) {
		return URI.create("https://pd." + shard + ".a.pvp.net");
	}

	private RiotAuth getRiotAuth(String accessToken, String entitlementsToken, String region) {
		String shard = determineShard(region);
		String authorization = "Bearer " + accessToken;

		VersionResponse versionResponse = valorantMetadataService.getVersion();
		String version = versionResponse.getData().getVersion();
		String clientVersion = versionResponse.getData().getRiotClientVersion();

		ClientPlatformRequest clientPlatformRequest = ClientPlatformRequest.builder()
				.platformType("PC")
				.platformOS("Windows")
				.platformOSVersion(version + "bit")
				.platformChipset("Unknown")
				.build();

		String clientPlatformB64Str = Base64.getEncoder().encodeToString(json.Marshal(clientPlatformRequest));

		return RiotAuth.builder().authorization(authorization).clientPlatform(clientPlatformB64Str)
				.clientVersion(clientVersion).entitlementsToken(entitlementsToken).shard(shard).build();
	}

	@Override
	@Cacheable(value = "getStorefront", key = "#puuid")
	public StorefrontResponse getStorefront(String puuid, String accessToken, String entitlementsToken, String region) {
		RiotAuth riotAuth = getRiotAuth(accessToken, entitlementsToken, region);

		log.debug("Fetching storefront for user {} on shard {}", puuid, riotAuth.getShard());

		return storeApiClient.getStorefront(getUrl(riotAuth.getShard()), puuid, "{}", riotAuth.getClientPlatform(),
				riotAuth.getClientVersion(), riotAuth.getEntitlementsToken(), riotAuth.getAuthorization());
	}

	@Override
	@Cacheable(value = "getDailyStores", key = "#puuid")
	public DailyStore getDailyStores(String puuid, String accessToken, String entitlementsToken, String region) {

		StorefrontResponse storefrontResponse = getStorefront(puuid, accessToken, entitlementsToken, region);
		List<DailyStore.DailyStoreItem> dailyStoreItems = new java.util.ArrayList<>();

		// Fetch all skins once to find parent skins for the levels
		WeaponSkinResponse allSkins = valorantMetadataService.getAllWeaponSkins();

		// Fetch all content tiers
		ContentTierResponse contentTiers = valorantMetadataService
				.getContentTiers();

		CurrencyResponse currency = valorantMetadataService.getCurrency("85ad13f7-3d1b-5128-9eb2-7cd8ee0b5741").getData();

		for (SingleItemStoreOffer dailyStoreLevelData : storefrontResponse.getSkinsPanelLayout()
				.getSingleItemStoreOffers()) {

			String itemID = dailyStoreLevelData.getRewards().get(0).getItemID();
			DailyStoreLevelResponse skinResponse = valorantMetadataService.getDailyStore(itemID);

			// Find parent skin
			WeaponSkinResponse.WeaponSkin parentSkin = allSkins.getData().stream()
					.filter(s -> s.getLevels() != null && s.getLevels().stream().anyMatch(l -> l.getUuid().equals(itemID)))
					.findFirst()
					.orElse(null);

			DailyStore.DailyStoreItem.DailyStoreItemBuilder itemBuilder = DailyStore.DailyStoreItem.builder()
					.uuid(skinResponse.getData().getUuid())
					.displayName(skinResponse.getData().getDisplayName())
					.levelItem(skinResponse.getData().getLevelItem())
					.displayIcon(skinResponse.getData().getDisplayIcon())
					.streamedVideo(skinResponse.getData().getStreamedVideo())
					.assetPath(skinResponse.getData().getAssetPath())
					.cost(dailyStoreLevelData.getCost() != null && !dailyStoreLevelData.getCost().isEmpty()
							? String.valueOf(dailyStoreLevelData.getCost().values().iterator().next())
							: null)
					.costIcon(currency.getDisplayIcon());

			if (parentSkin != null) {
				if (parentSkin.getLevels() != null) {
					List<DailyStore.DailyStoreItem.Level> levels = parentSkin.getLevels().stream()
							.map(l -> DailyStore.DailyStoreItem.Level.builder()
									.uuid(l.getUuid())
									.displayName(l.getDisplayName())
									.displayIcon(l.getDisplayIcon())
									.streamedVideo(l.getStreamedVideo())
									.build())
							.collect(Collectors.toList());
					itemBuilder.levels(levels);
				}

				if (parentSkin.getChromas() != null) {
					List<DailyStore.DailyStoreItem.Chroma> chromas = parentSkin.getChromas().stream()
							.map(c -> DailyStore.DailyStoreItem.Chroma.builder()
									.uuid(c.getUuid())
									.displayName(c.getDisplayName())
									.displayIcon(c.getDisplayIcon())
									.swatch(c.getSwatch())
									.streamedVideo(c.getStreamedVideo())
									.build())
							.collect(Collectors.toList());
					itemBuilder.chromas(chromas);
				}

				if (parentSkin.getContentTierUuid() != null && contentTiers != null && contentTiers.getData() != null) {
					contentTiers.getData().stream()
							.filter(t -> t.getUuid().equals(parentSkin.getContentTierUuid()))
							.findFirst()
							.ifPresent(tier -> {
								itemBuilder.tier(DailyStore.DailyStoreItem.Tier.builder()
										.uuid(tier.getUuid())
										.displayName(tier.getDisplayName())
										.displayIcon(tier.getDisplayIcon())
										.highlightColor(tier.getHighlightColor())
										.build());
							});
				}
			}

			dailyStoreItems.add(itemBuilder.build());
		}

		long remainingDurationSeconds = storefrontResponse.getSkinsPanelLayout()
				.getSingleItemOffersRemainingDurationInSeconds();
		Long expireAt = (System.currentTimeMillis() / 1000) + remainingDurationSeconds;

		return DailyStore.builder()
				.items(dailyStoreItems)
				.expireAt(expireAt)
				.build();
	}

	@Override
	@Cacheable(value = "getPlayerName", key = "#puuid")
	public List<PlayerNameResponse> getPlayerName(String puuid, String accessToken, String entitlementsToken,
			String region) {
		RiotAuth riotAuth = getRiotAuth(accessToken, entitlementsToken, region);

		String response = storeApiClient.getPlayerName(getUrl(riotAuth.getShard()),
				Collections.singletonList(puuid),
				riotAuth.getClientPlatform(), riotAuth.getClientVersion(), riotAuth.getEntitlementsToken(),
				riotAuth.getAuthorization());

		return json.Unmarshal(response, new TypeReference<List<PlayerNameResponse>>() {
		});

	}

	@Override
	@Cacheable(value = "getNightMarket", key = "#puuid")
	public NightMarket getNightMarket(String puuid, String accessToken, String entitlementsToken, String region) {
		StorefrontResponse storefrontResponse = getStorefront(puuid, accessToken, entitlementsToken, region);
		List<NightMarket.NightMarketItem> nightMarketItems = new java.util.ArrayList<>();

		if (storefrontResponse.getBonusStore() == null
				|| storefrontResponse.getBonusStore().getBonusStoreOffers() == null) {
			return NightMarket.builder().items(Collections.emptyList()).build();
		}

		WeaponSkinResponse allSkins = valorantMetadataService.getAllWeaponSkins();
		ContentTierResponse contentTiers = valorantMetadataService
				.getContentTiers();
		CurrencyResponse currency = valorantMetadataService.getCurrency("85ad13f7-3d1b-5128-9eb2-7cd8ee0b5741").getData();

		for (StorefrontResponse.BonusStoreOffer bonusOffer : storefrontResponse.getBonusStore().getBonusStoreOffers()) {
			String itemID = bonusOffer.getOffer().getRewards().get(0).getItemID();
			DailyStoreLevelResponse skinResponse = valorantMetadataService.getDailyStore(itemID);

			// Find parent skin
			WeaponSkinResponse.WeaponSkin parentSkin = allSkins.getData().stream()
					.filter(s -> s.getLevels() != null && s.getLevels().stream().anyMatch(l -> l.getUuid().equals(itemID)))
					.findFirst()
					.orElse(null);

			NightMarket.NightMarketItem.NightMarketItemBuilder itemBuilder = NightMarket.NightMarketItem.builder()
					.uuid(skinResponse.getData().getUuid())
					.displayName(skinResponse.getData().getDisplayName())
					.levelItem(skinResponse.getData().getLevelItem())
					.displayIcon(skinResponse.getData().getDisplayIcon())
					.streamedVideo(skinResponse.getData().getStreamedVideo())
					.assetPath(skinResponse.getData().getAssetPath())
					.costIcon(currency.getDisplayIcon())
					.originalCost(String.valueOf(bonusOffer.getOffer().getCost().values().iterator().next()))
					.discountedCost(String.valueOf(bonusOffer.getDiscountCosts().values().iterator().next()))
					.discountPercent(bonusOffer.getDiscountPercent());

			if (parentSkin != null) {
				if (parentSkin.getLevels() != null) {
					List<NightMarket.NightMarketItem.Level> levels = parentSkin.getLevels().stream()
							.map(l -> NightMarket.NightMarketItem.Level.builder()
									.uuid(l.getUuid())
									.displayName(l.getDisplayName())
									.displayIcon(l.getDisplayIcon())
									.streamedVideo(l.getStreamedVideo())
									.build())
							.collect(Collectors.toList());
					itemBuilder.levels(levels);
				}

				if (parentSkin.getChromas() != null) {
					List<NightMarket.NightMarketItem.Chroma> chromas = parentSkin.getChromas().stream()
							.map(c -> NightMarket.NightMarketItem.Chroma.builder()
									.uuid(c.getUuid())
									.displayName(c.getDisplayName())
									.displayIcon(c.getDisplayIcon())
									.swatch(c.getSwatch())
									.streamedVideo(c.getStreamedVideo())
									.build())
							.collect(Collectors.toList());
					itemBuilder.chromas(chromas);
				}

				if (parentSkin.getContentTierUuid() != null && contentTiers != null && contentTiers.getData() != null) {
					contentTiers.getData().stream()
							.filter(t -> t.getUuid().equals(parentSkin.getContentTierUuid()))
							.findFirst()
							.ifPresent(tier -> {
								itemBuilder.tier(NightMarket.NightMarketItem.Tier.builder()
										.uuid(tier.getUuid())
										.displayName(tier.getDisplayName())
										.displayIcon(tier.getDisplayIcon())
										.highlightColor(tier.getHighlightColor())
										.build());
							});
				}
			}

			nightMarketItems.add(itemBuilder.build());
		}

		long remainingDurationSeconds = storefrontResponse.getBonusStore().getBonusStoreRemainingDurationInSeconds();
		Long expireAt = (System.currentTimeMillis() / 1000) + remainingDurationSeconds;

		return NightMarket.builder()
				.items(nightMarketItems)
				.expireAt(expireAt)
				.build();
	}

	@Override
	@Cacheable(value = "getMatchHistory", key = "#puuid + '-' + #startIndex + '-' + #endIndex")
	public MatchHistory getMatchHistory(String puuid, int startIndex, int endIndex, String accessToken,
			String entitlementsToken, String region) {
		RiotAuth riotAuth = getRiotAuth(accessToken, entitlementsToken, region);

		AgentsResponse agents = valorantMetadataService.getAgents();
		MapsResponse maps = valorantMetadataService.getMaps();
		CompetitiveTiersResponse tiers = valorantMetadataService.getCompetitiveTiers();

		MatchHistoryResponse historyResponse = storeApiClient.getMatchHistory(getUrl(riotAuth.getShard()), puuid,
				startIndex, endIndex,
				riotAuth.getClientPlatform(), riotAuth.getClientVersion(), riotAuth.getEntitlementsToken(),
				riotAuth.getAuthorization());

		List<MatchHistory.Match> matches = new ArrayList<>();

		if (historyResponse != null && historyResponse.getHistory() != null) {
			for (MatchHistoryResponse.History historyItem : historyResponse.getHistory()) {
				try {
					MatchDetailsResponse details = storeApiClient.getMatchDetails(getUrl(riotAuth.getShard()),
							historyItem.getMatchID(), riotAuth.getClientPlatform(), riotAuth.getClientVersion(),
							riotAuth.getEntitlementsToken(), riotAuth.getAuthorization());

					if (details == null || details.getPlayers() == null)
						continue;

					MatchDetailsResponse.Player player = details.getPlayers().stream()
							.filter(p -> p.getSubject().equalsIgnoreCase(puuid))
							.findFirst().orElse(null);

					if (player == null)
						continue;

					// Agent
					String agentName = "Unknown";
					String agentIcon = "";
					if (agents != null && agents.getData() != null) {
						String charId = player.getCharacterId();
						AgentsResponse.Agent agent = agents.getData().stream()
								.filter(a -> a.getUuid().equalsIgnoreCase(charId))
								.findFirst().orElse(null);
						if (agent != null) {
							agentName = agent.getDisplayName();
							agentIcon = agent.getDisplayIcon();
						}
					}

					// Map
					String mapName = "Unknown";
					String mapPremier = "";
					String mapBg = "";
					if (maps != null && maps.getData() != null) {
						String mapId = details.getMatchInfo().getMapId();
						MapsResponse.ValorantMap map = maps.getData().stream()
								.filter(m -> m.getMapUrl().equalsIgnoreCase(mapId))
								.findFirst().orElse(null);
						if (map != null) {
							mapName = map.getDisplayName();
							mapBg = map.getStylizedBackgroundImage();
							mapPremier = map.getPremierBackgroundImage();
						}
					}

					// Result & Score
					String result = "DRAW"; // Default
					String score = "0 - 0";
					String teamId = player.getTeamId();

					MatchDetailsResponse.Team myTeam = details.getTeams().stream()
							.filter(t -> t.getTeamId().equals(teamId)).findFirst().orElse(null);
					MatchDetailsResponse.Team enemyTeam = details.getTeams().stream()
							.filter(t -> !t.getTeamId().equals(teamId)).findFirst().orElse(null);

					if (myTeam != null) {
						if (myTeam.isWon()) {
							result = "VICTORY";
							mapBg = mapPremier;
						} else if (enemyTeam != null && enemyTeam.isWon()) {
							result = "DEFEAT";
						}

						int myScore = myTeam.getRoundsWon();
						int enemyScore = enemyTeam != null ? enemyTeam.getRoundsWon() : 0;
						score = myScore + " - " + enemyScore;
					}

					// KDA
					String kda = "0 / 0 / 0";
					if (player.getStats() != null) {
						kda = player.getStats().getKills() + " / " + player.getStats().getDeaths() + " / "
								+ player.getStats().getAssists();
					}

					// Rank
					String rankName = "Unrated";
					String rankIcon = "";
					if (details.getMatchInfo().isRanked() && tiers != null && tiers.getData() != null
							&& !tiers.getData().isEmpty()) {
						CompetitiveTiersResponse.TierSet tierSet = tiers.getData().get(tiers.getData().size() - 1);
						CompetitiveTiersResponse.Tier tier = tierSet.getTiers().stream()
								.filter(t -> t.getTier() == player.getCompetitiveTier())
								.findFirst().orElse(null);
						if (tier != null) {
							rankName = tier.getTierName();
							rankIcon = tier.getLargeIcon();
						}
					}

					// Players list for details
					List<MatchHistory.MatchPlayer> matchPlayers = new ArrayList<>();
					int maxScore = 0;
					int maxBlueScore = 0;
					int maxRedScore = 0;

					// First pass to find max scores
					for (MatchDetailsResponse.Player p : details.getPlayers()) {
						if (p.getStats() == null)
							continue;
						int s = p.getStats().getScore();
						if (s > maxScore)
							maxScore = s;
						if ("Blue".equalsIgnoreCase(p.getTeamId())) {
							if (s > maxBlueScore)
								maxBlueScore = s;
						} else if ("Red".equalsIgnoreCase(p.getTeamId())) {
							if (s > maxRedScore)
								maxRedScore = s;
						}
					}

					for (MatchDetailsResponse.Player p : details.getPlayers()) {
						String pAgentName = "Unknown";
						String pAgentIcon = "";
						if (agents != null && agents.getData() != null) {
							String charId = p.getCharacterId();
							AgentsResponse.Agent agent = agents.getData().stream()
									.filter(a -> a.getUuid().equalsIgnoreCase(charId))
									.findFirst().orElse(null);
							if (agent != null) {
								pAgentName = agent.getDisplayName();
								pAgentIcon = agent.getDisplayIcon();
							}
						}

						String pRankName = "Unrated";
						String pRankIcon = "";
						if (tiers != null && tiers.getData() != null && !tiers.getData().isEmpty()) {
							CompetitiveTiersResponse.TierSet tierSet = tiers.getData().get(tiers.getData().size() - 1);
							CompetitiveTiersResponse.Tier tier = tierSet.getTiers().stream()
									.filter(t -> t.getTier() == p.getCompetitiveTier())
									.findFirst().orElse(null);
							if (tier != null) {
								pRankName = tier.getTierName();
								pRankIcon = tier.getSmallIcon();
							}
						}

						int pScore = p.getStats() != null ? p.getStats().getScore() : 0;
						boolean isMatchMVP = pScore > 0 && pScore == maxScore;
						boolean isTeamMVP = false;
						if (pScore > 0) {
							if ("Blue".equalsIgnoreCase(p.getTeamId())) {
								isTeamMVP = pScore == maxBlueScore;
							} else if ("Red".equalsIgnoreCase(p.getTeamId())) {
								isTeamMVP = pScore == maxRedScore;
							}
						}

						matchPlayers.add(MatchHistory.MatchPlayer.builder()
								.subject(p.getSubject())
								.gameName(p.getGameName())
								.tagLine(p.getTagLine())
								.teamId(p.getTeamId())
								.agentName(pAgentName)
								.agentIcon(pAgentIcon)
								.kills(p.getStats() != null ? p.getStats().getKills() : 0)
								.deaths(p.getStats() != null ? p.getStats().getDeaths() : 0)
								.assists(p.getStats() != null ? p.getStats().getAssists() : 0)
								.score(pScore)
								.rankName(pRankName)
								.rankIcon(pRankIcon)
								.matchMVP(isMatchMVP)
								.teamMVP(isTeamMVP)
								.build());
					}

					// User MVP status
					int s = player.getStats() != null ? player.getStats().getScore() : 0;
					boolean userMatchMVP = s > 0 && s == maxScore;
					boolean userTeamMVP = false;
					if (s > 0) {
						if ("Blue".equalsIgnoreCase(player.getTeamId())) {
							userTeamMVP = s == maxBlueScore;
						} else if ("Red".equalsIgnoreCase(player.getTeamId())) {
							userTeamMVP = s == maxRedScore;
						}
					}

					matches.add(MatchHistory.Match.builder()
							.matchId(historyItem.getMatchID())
							.gameStartTime(details.getMatchInfo().getGameStartMillis())
							.durationMillis(details.getMatchInfo().getGameLengthMillis())
							.mapName(mapName)
							.mapBg(mapBg)
							.agentName(agentName)
							.agentIcon(agentIcon)
							.result(result)
							.score(score)
							.kda(kda)
							.rankName(rankName)
							.rankIcon(rankIcon)
							.isRanked(details.getMatchInfo().isRanked())
							.queueID(details.getMatchInfo().getQueueID())
							.gameMode(details.getMatchInfo().getGameMode())
							.matchMVP(userMatchMVP)
							.teamMVP(userTeamMVP)
							.players(matchPlayers)
							.build());

				} catch (Exception e) {
					log.error("Error processing match " + historyItem.getMatchID(), e);
				}
			}
		}

		return MatchHistory.builder()
				.matches(matches)
				.beginIndex(historyResponse.getBeginIndex())
				.endIndex(historyResponse.getEndIndex())
				.total(historyResponse.getTotal())
				.build();
	}

	@Override
	@Cacheable(value = "getWallet", key = "#puuid")
	public WalletResponse getWallet(String puuid, String accessToken, String entitlementsToken, String region) {
		RiotAuth riotAuth = getRiotAuth(accessToken, entitlementsToken, region);

		return storeApiClient.getWallet(getUrl(riotAuth.getShard()), puuid, riotAuth.getClientPlatform(),
				riotAuth.getClientVersion(), riotAuth.getEntitlementsToken(), riotAuth.getAuthorization());
	}
}
