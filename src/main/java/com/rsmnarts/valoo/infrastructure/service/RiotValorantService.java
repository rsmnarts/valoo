package com.rsmnarts.valoo.infrastructure.service;

import java.net.URI;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
import com.rsmnarts.valoo.infrastructure.client.dto.CompetitiveUpdatesResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.ContentTierResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.CurrencyResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.DailyStoreLevelResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.MapsResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.MatchDetailsResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.MatchHistoryResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.PlayerNameResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.StorefrontResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.VersionResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.WalletResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.WeaponSkinResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RiotValorantService implements RiotValorantUseCase {

	private final RiotValorantApiClient storeApiClient;
	private final ValorantMetadataService valorantMetadataService;
	private final RiotMatchDataService riotMatchDataService;

	public RiotValorantService(RiotValorantApiClient storeApiClient, ValorantMetadataService valorantMetadataService,
			RiotMatchDataService riotMatchDataService) {
		this.storeApiClient = storeApiClient;
		this.valorantMetadataService = valorantMetadataService;
		this.riotMatchDataService = riotMatchDataService;
	}

	private String determineShard(String region) {
		if (region == null)
			return "ap";
		String lowerRegion = region.toLowerCase();
		if (List.of("na", "latam", "br").contains(lowerRegion))
			return "na";
		if (List.of("pbe", "eu", "ap", "kr").contains(lowerRegion))
			return lowerRegion;
		return "ap";
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
	@Cacheable(value = "getStorefront", key = "#puuid", unless = "#result == null")
	public StorefrontResponse getStorefront(String puuid, String accessToken, String entitlementsToken, String region) {
		RiotAuth riotAuth = getRiotAuth(accessToken, entitlementsToken, region);

		log.debug("Fetching storefront for user {} on shard {}", puuid, riotAuth.getShard());

		return storeApiClient.getStorefront(getUrl(riotAuth.getShard()), puuid, "{}", riotAuth.getClientPlatform(),
				riotAuth.getClientVersion(), riotAuth.getEntitlementsToken(), riotAuth.getAuthorization());
	}

	@Override
	@Cacheable(value = "getDailyStores", key = "#puuid", unless = "#result == null")
	public DailyStore getDailyStores(String puuid, String accessToken, String entitlementsToken, String region) {
		StorefrontResponse storefront = getStorefront(puuid, accessToken, entitlementsToken, region);
		if (storefront == null || storefront.getSkinsPanelLayout() == null)
			return null;

		// 1. Fetch Metadata once
		WeaponSkinResponse allSkins = valorantMetadataService.getAllWeaponSkins();
		ContentTierResponse contentTiers = valorantMetadataService.getContentTiers();
		CurrencyResponse currency = valorantMetadataService.getCurrency("85ad13f7-3d1b-5128-9eb2-7cd8ee0b5741").getData();

		// 2. Map all skins for parent lookup
		Map<String, WeaponSkinResponse.WeaponSkin> skinByLevelMap = allSkins.getData().stream()
				.flatMap(skin -> skin.getLevels() != null ? skin.getLevels().stream().map(l -> Map.entry(l.getUuid(), skin))
						: Stream.empty())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (s1, s2) -> s1));

		Map<String, ContentTierResponse.ContentTier> tierMap = contentTiers.getData().stream()
				.collect(Collectors.toMap(ContentTierResponse.ContentTier::getUuid, t -> t, (t1, t2) -> t1));

		// 3. Parallel fetch skin details for daily offers
		List<CompletableFuture<DailyStore.DailyStoreItem>> futures = storefront.getSkinsPanelLayout()
				.getSingleItemStoreOffers().stream()
				.map(offer -> CompletableFuture.supplyAsync(() -> {
					String itemID = offer.getRewards().get(0).getItemID();
					DailyStoreLevelResponse skinLevel = valorantMetadataService.getDailyStore(itemID);
					WeaponSkinResponse.WeaponSkin parentSkin = skinByLevelMap.get(itemID);

					DailyStore.DailyStoreItem.DailyStoreItemBuilder builder = DailyStore.DailyStoreItem.builder()
							.uuid(skinLevel.getData().getUuid())
							.displayName(skinLevel.getData().getDisplayName())
							.displayIcon(skinLevel.getData().getDisplayIcon())
							.streamedVideo(skinLevel.getData().getStreamedVideo())
							.assetPath(skinLevel.getData().getAssetPath())
							.cost(offer.getCost() != null && !offer.getCost().isEmpty()
									? String.valueOf(offer.getCost().values().iterator().next())
									: null)
							.costIcon(currency.getDisplayIcon());

					if (parentSkin != null) {
						if (parentSkin.getLevels() != null) {
							builder.levels(parentSkin.getLevels().stream().map(l -> DailyStore.DailyStoreItem.Level.builder()
									.uuid(l.getUuid()).displayName(l.getDisplayName()).displayIcon(l.getDisplayIcon())
									.streamedVideo(l.getStreamedVideo()).build()).toList());
						}
						if (parentSkin.getChromas() != null) {
							builder.chromas(parentSkin.getChromas().stream().map(c -> DailyStore.DailyStoreItem.Chroma.builder()
									.uuid(c.getUuid()).displayName(c.getDisplayName()).displayIcon(c.getDisplayIcon())
									.swatch(c.getSwatch()).streamedVideo(c.getStreamedVideo()).build()).toList());
						}
						if (parentSkin.getContentTierUuid() != null) {
							ContentTierResponse.ContentTier tier = tierMap.get(parentSkin.getContentTierUuid());
							if (tier != null) {
								builder.tier(
										DailyStore.DailyStoreItem.Tier.builder().uuid(tier.getUuid()).displayName(tier.getDisplayName())
												.displayIcon(tier.getDisplayIcon()).highlightColor(tier.getHighlightColor()).build());
							}
						}
					}
					return builder.build();
				}))
				.toList();

		long remaining = storefront.getSkinsPanelLayout().getSingleItemOffersRemainingDurationInSeconds();
		return DailyStore.builder()
				.items(futures.stream().map(CompletableFuture::join).toList())
				.expireAt((System.currentTimeMillis() / 1000) + remaining)
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
	@Cacheable(value = "getNightMarket", key = "#puuid", unless = "#result == null")
	public NightMarket getNightMarket(String puuid, String accessToken, String entitlementsToken, String region) {
		StorefrontResponse storefront = getStorefront(puuid, accessToken, entitlementsToken, region);
		if (storefront == null || storefront.getBonusStore() == null
				|| storefront.getBonusStore().getBonusStoreOffers() == null) {
			return NightMarket.builder().items(Collections.emptyList()).build();
		}

		WeaponSkinResponse allSkins = valorantMetadataService.getAllWeaponSkins();
		ContentTierResponse contentTiers = valorantMetadataService.getContentTiers();
		CurrencyResponse currency = valorantMetadataService.getCurrency("85ad13f7-3d1b-5128-9eb2-7cd8ee0b5741").getData();

		Map<String, WeaponSkinResponse.WeaponSkin> skinByLevelMap = allSkins.getData().stream()
				.flatMap(skin -> skin.getLevels() != null ? skin.getLevels().stream().map(l -> Map.entry(l.getUuid(), skin))
						: Stream.empty())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (s1, s2) -> s1));

		Map<String, ContentTierResponse.ContentTier> tierMap = contentTiers.getData().stream()
				.collect(Collectors.toMap(ContentTierResponse.ContentTier::getUuid, t -> t, (t1, t2) -> t1));

		List<CompletableFuture<NightMarket.NightMarketItem>> futures = storefront.getBonusStore().getBonusStoreOffers()
				.stream()
				.map(bonus -> CompletableFuture.supplyAsync(() -> {
					String itemID = bonus.getOffer().getRewards().get(0).getItemID();
					DailyStoreLevelResponse skinLevel = valorantMetadataService.getDailyStore(itemID);
					WeaponSkinResponse.WeaponSkin parentSkin = skinByLevelMap.get(itemID);

					NightMarket.NightMarketItem.NightMarketItemBuilder builder = NightMarket.NightMarketItem.builder()
							.uuid(skinLevel.getData().getUuid())
							.displayName(skinLevel.getData().getDisplayName())
							.displayIcon(skinLevel.getData().getDisplayIcon())
							.streamedVideo(skinLevel.getData().getStreamedVideo())
							.assetPath(skinLevel.getData().getAssetPath())
							.costIcon(currency.getDisplayIcon())
							.originalCost(String.valueOf(bonus.getOffer().getCost().values().iterator().next()))
							.discountedCost(String.valueOf(bonus.getDiscountCosts().values().iterator().next()))
							.discountPercent(bonus.getDiscountPercent());

					if (parentSkin != null) {
						if (parentSkin.getLevels() != null) {
							builder.levels(parentSkin.getLevels().stream().map(l -> NightMarket.NightMarketItem.Level.builder()
									.uuid(l.getUuid()).displayName(l.getDisplayName()).displayIcon(l.getDisplayIcon())
									.streamedVideo(l.getStreamedVideo()).build()).toList());
						}
						if (parentSkin.getChromas() != null) {
							builder.chromas(parentSkin.getChromas().stream().map(c -> NightMarket.NightMarketItem.Chroma.builder()
									.uuid(c.getUuid()).displayName(c.getDisplayName()).displayIcon(c.getDisplayIcon())
									.swatch(c.getSwatch()).streamedVideo(c.getStreamedVideo()).build()).toList());
						}
						if (parentSkin.getContentTierUuid() != null) {
							ContentTierResponse.ContentTier tier = tierMap.get(parentSkin.getContentTierUuid());
							if (tier != null) {
								builder.tier(
										NightMarket.NightMarketItem.Tier.builder().uuid(tier.getUuid()).displayName(tier.getDisplayName())
												.displayIcon(tier.getDisplayIcon()).highlightColor(tier.getHighlightColor()).build());
							}
						}
					}
					return builder.build();
				}))
				.toList();

		long remaining = storefront.getBonusStore().getBonusStoreRemainingDurationInSeconds();
		return NightMarket.builder()
				.items(futures.stream().map(CompletableFuture::join).toList())
				.expireAt((System.currentTimeMillis() / 1000) + remaining)
				.build();
	}

	@Override
	@Cacheable(value = "match-history-list", key = "#puuid + '-' + #startIndex + '-' + #endIndex", unless = "#result == null")
	public MatchHistory getMatchHistory(String puuid, int startIndex, int endIndex, String accessToken,
			String entitlementsToken, String region) {
		RiotAuth riotAuth = getRiotAuth(accessToken, entitlementsToken, region);
		URI uri = getUrl(riotAuth.getShard());

		// 1. Fetch Metadata and cache as Maps for O(1) lookup
		Map<String, AgentsResponse.Agent> agentMap = valorantMetadataService.getAgents().getData().stream()
				.collect(Collectors.toMap(a -> a.getUuid().toLowerCase(), a -> a, (a1, a2) -> a1));
		Map<String, MapsResponse.ValorantMap> mapMap = valorantMetadataService.getMaps().getData().stream()
				.collect(Collectors.toMap(m -> m.getMapUrl().toLowerCase(), m -> m, (m1, m2) -> m1));

		CompetitiveTiersResponse tiersResponse = valorantMetadataService.getCompetitiveTiers();
		List<CompetitiveTiersResponse.Tier> allTiers = tiersResponse.getData().get(tiersResponse.getData().size() - 1)
				.getTiers();
		Map<Integer, CompetitiveTiersResponse.Tier> tierMap = allTiers.stream()
				.collect(Collectors.toMap(CompetitiveTiersResponse.Tier::getTier, t -> t, (t1, t2) -> t1));

		// 2. Fetch Match History List
		MatchHistoryResponse historyResponse = storeApiClient.getMatchHistory(uri, puuid, startIndex, endIndex,
				riotAuth.getClientPlatform(), riotAuth.getClientVersion(), riotAuth.getEntitlementsToken(),
				riotAuth.getAuthorization());

		if (historyResponse == null || historyResponse.getHistory() == null) {
			return MatchHistory.builder().matches(Collections.emptyList()).build();
		}

		// 3. Parallel fetch match details
		List<CompletableFuture<MatchHistory.Match>> matchFutures = historyResponse.getHistory().stream()
				.map(historyItem -> CompletableFuture.supplyAsync(() -> {
					try {
						MatchDetailsResponse details = riotMatchDataService.getMatchDetails(uri, historyItem.getMatchID(),
								riotAuth.getClientPlatform(), riotAuth.getClientVersion(), riotAuth.getEntitlementsToken(),
								riotAuth.getAuthorization());
						if (details == null)
							return null;
						return processMatchDetails(puuid, details, agentMap, mapMap, tierMap, riotAuth, uri);
					} catch (Exception e) {
						log.error("Error fetching details for match {}: {}", historyItem.getMatchID(), e.getMessage());
						return null;
					}
				}))
				.collect(Collectors.toList());

		List<MatchHistory.Match> matches = matchFutures.stream()
				.map(CompletableFuture::join)
				.filter(java.util.Objects::nonNull)
				.collect(Collectors.toList());

		return MatchHistory.builder()
				.matches(matches)
				.beginIndex(historyResponse.getBeginIndex())
				.endIndex(historyResponse.getEndIndex())
				.total(historyResponse.getTotal())
				.build();
	}

	private MatchHistory.Match processMatchDetails(String puuid, MatchDetailsResponse details,
			Map<String, AgentsResponse.Agent> agentMap, Map<String, MapsResponse.ValorantMap> mapMap,
			Map<Integer, CompetitiveTiersResponse.Tier> tierMap, RiotAuth riotAuth, URI uri) {

		MatchDetailsResponse.Player player = details.getPlayers().stream()
				.filter(p -> p.getSubject().equalsIgnoreCase(puuid))
				.findFirst().orElse(null);
		if (player == null)
			return null;

		// Agent & Map info
		AgentsResponse.Agent agent = agentMap.get(player.getCharacterId().toLowerCase());
		MapsResponse.ValorantMap valorantMap = mapMap.get(details.getMatchInfo().getMapId().toLowerCase());

		// Score & Result
		MatchDetailsResponse.Team myTeam = details.getTeams().stream().filter(t -> t.getTeamId().equals(player.getTeamId()))
				.findFirst().orElse(null);
		MatchDetailsResponse.Team enemyTeam = details.getTeams().stream()
				.filter(t -> !t.getTeamId().equals(player.getTeamId())).findFirst().orElse(null);
		String result = myTeam != null && Boolean.TRUE.equals(myTeam.getWon()) ? "VICTORY"
				: (enemyTeam != null && Boolean.TRUE.equals(enemyTeam.getWon()) ? "DEFEAT" : "DRAW");
		String score = (myTeam != null ? myTeam.getRoundsWon() : 0) + " - "
				+ (enemyTeam != null ? enemyTeam.getRoundsWon() : 0);

		// MVP Logic
		int maxScore = details.getPlayers().stream().mapToInt(p -> p.getStats() != null ? p.getStats().getScore() : 0).max()
				.orElse(0);
		int teamMaxScore = details.getPlayers().stream()
				.filter(p -> p.getTeamId().equals(player.getTeamId()))
				.mapToInt(p -> p.getStats() != null ? p.getStats().getScore() : 0)
				.max().orElse(0);
		int userScore = player.getStats() != null ? player.getStats().getScore() : 0;

		// Parallel fetch RR for all players in this match
		List<CompletableFuture<MatchHistory.MatchPlayer>> playerFutures = details.getPlayers().stream()
				.map(p -> CompletableFuture.supplyAsync(() -> {
					AgentsResponse.Agent pAgent = agentMap.get(p.getCharacterId().toLowerCase());
					CompetitiveTiersResponse.Tier pTier = tierMap.get(p.getCompetitiveTier());
					int pScore = p.getStats() != null ? p.getStats().getScore() : 0;

					Integer rrChange = null;
					if (Boolean.TRUE.equals(details.getMatchInfo().getIsRanked())) {
						rrChange = riotMatchDataService.getRankedRatingEarned(uri, p.getSubject(),
								details.getMatchInfo().getMatchId(),
								riotAuth.getClientPlatform(), riotAuth.getClientVersion(), riotAuth.getEntitlementsToken(),
								riotAuth.getAuthorization());
					}

					return MatchHistory.MatchPlayer.builder()
							.subject(p.getSubject()).gameName(p.getGameName()).tagLine(p.getTagLine()).teamId(p.getTeamId())
							.agentName(pAgent != null ? pAgent.getDisplayName() : "Unknown")
							.agentIcon(pAgent != null ? pAgent.getDisplayIcon() : "")
							.kills(p.getStats() != null ? p.getStats().getKills() : 0)
							.deaths(p.getStats() != null ? p.getStats().getDeaths() : 0)
							.assists(p.getStats() != null ? p.getStats().getAssists() : 0)
							.score(pScore).rankName(pTier != null ? pTier.getTierName() : "Unrated")
							.rankIcon(pTier != null ? pTier.getSmallIcon() : "")
							.matchMVP(pScore > 0 && pScore == maxScore)
							.teamMVP(pScore > 0 && pScore == (details.getPlayers().stream()
									.filter(tp -> tp.getTeamId().equals(p.getTeamId()))
									.mapToInt(tp -> tp.getStats() != null ? tp.getStats().getScore() : 0).max().orElse(0)))
							.rankedRatingEarned(rrChange)
							.build();
				}))
				.collect(Collectors.toList());

		List<MatchHistory.MatchPlayer> matchPlayers = playerFutures.stream().map(CompletableFuture::join)
				.collect(Collectors.toList());
		Integer userRR = matchPlayers.stream().filter(mp -> mp.getSubject().equalsIgnoreCase(puuid))
				.map(MatchHistory.MatchPlayer::getRankedRatingEarned).findFirst().orElse(null);

		CompetitiveTiersResponse.Tier userTier = tierMap.get(player.getCompetitiveTier());

		return MatchHistory.Match.builder()
				.matchId(details.getMatchInfo().getMatchId())
				.gameStartTime(details.getMatchInfo().getGameStartMillis())
				.durationMillis(details.getMatchInfo().getGameLengthMillis())
				.mapName(valorantMap != null ? valorantMap.getDisplayName() : "Unknown")
				.mapBg(result.equals("VICTORY") && valorantMap != null ? valorantMap.getPremierBackgroundImage()
						: (valorantMap != null ? valorantMap.getStylizedBackgroundImage() : ""))
				.agentName(agent != null ? agent.getDisplayName() : "Unknown")
				.agentIcon(agent != null ? agent.getDisplayIcon() : "")
				.result(result).score(score)
				.kda(player.getStats().getKills() + " / " + player.getStats().getDeaths() + " / "
						+ player.getStats().getAssists())
				.rankName(userTier != null ? userTier.getTierName() : "Unrated")
				.rankIcon(userTier != null ? userTier.getLargeIcon() : "")
				.isRanked(Boolean.TRUE.equals(details.getMatchInfo().getIsRanked()))
				.queueID(details.getMatchInfo().getQueueID())
				.gameMode(details.getMatchInfo().getGameMode())
				.matchMVP(userScore > 0 && userScore == maxScore).teamMVP(userScore > 0 && userScore == teamMaxScore)
				.rankedRatingEarned(userRR).players(matchPlayers)
				.build();
	}

	@Override
	@Cacheable(value = "getWallet", key = "#puuid")
	public WalletResponse getWallet(String puuid, String accessToken, String entitlementsToken, String region) {
		RiotAuth riotAuth = getRiotAuth(accessToken, entitlementsToken, region);

		return storeApiClient.getWallet(getUrl(riotAuth.getShard()), puuid, riotAuth.getClientPlatform(),
				riotAuth.getClientVersion(), riotAuth.getEntitlementsToken(), riotAuth.getAuthorization());
	}

	@Override
	@Cacheable(value = "getPlayer", key = "#puuid")
	public PlayerNameResponse getPlayer(String puuid, String accessToken, String entitlementsToken, String region) {
		List<PlayerNameResponse> players = getPlayerName(puuid, accessToken, entitlementsToken, region);
		if (players != null && !players.isEmpty()) {
			return players.get(0);
		}
		return null;
	}

	@Override
	@Cacheable(value = "getCompetitiveUpdates", key = "#puuid")
	public CompetitiveUpdatesResponse getCompetitiveUpdates(String puuid, String accessToken, String entitlementsToken,
			String region) {
		RiotAuth riotAuth = getRiotAuth(accessToken, entitlementsToken, region);

		String response = storeApiClient.getCompetitiveUpdates(getUrl(riotAuth.getShard()), puuid, 0, 5, "competitive",
				riotAuth.getClientPlatform(), riotAuth.getClientVersion(), riotAuth.getEntitlementsToken(),
				riotAuth.getAuthorization());

		return json.Unmarshal(response, CompetitiveUpdatesResponse.class);
	}

	@Override
	@Caching(evict = {
			@CacheEvict(value = "match-history-list", allEntries = true),
			@CacheEvict(value = "match-details", allEntries = true),
			@CacheEvict(value = "competitive-updates-map", allEntries = true)
	})
	public void forceRefreshMatchHistory(String puuid, String accessToken, String entitlementsToken, String region) {
		log.info("Force refreshing match history cache for user: {}", puuid);
	}
}
