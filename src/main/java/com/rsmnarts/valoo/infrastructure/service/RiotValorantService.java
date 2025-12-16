package com.rsmnarts.valoo.infrastructure.service;

import java.net.URI;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rsmnarts.valoo.common.utils.json;
import com.rsmnarts.valoo.domain.model.DailyStore;
import com.rsmnarts.valoo.domain.model.NightMarket;
import com.rsmnarts.valoo.domain.model.RiotAuth;
import com.rsmnarts.valoo.domain.usecase.RiotValorantUseCase;
import com.rsmnarts.valoo.infrastructure.client.RiotValorantApiClient;
import com.rsmnarts.valoo.infrastructure.client.dto.ClientPlatformRequest;
import com.rsmnarts.valoo.infrastructure.client.dto.ContentTierResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.DailyStoreLevelResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.PlayerNameResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.StorefrontResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.StorefrontResponse.SingleItemStoreOffer;
import com.rsmnarts.valoo.infrastructure.client.dto.VersionResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.WeaponSkinResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiotValorantService implements RiotValorantUseCase {

	private final RiotValorantApiClient storeApiClient;
	private final ValorantMetadataService valorantMetadataService;

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
	public StorefrontResponse getStorefront(String puuid, String accessToken, String entitlementsToken, String region) {
		RiotAuth riotAuth = getRiotAuth(accessToken, entitlementsToken, region);

		log.debug("Fetching storefront for user {} on shard {}", puuid, riotAuth.getShard());

		return storeApiClient.getStorefront(getUrl(riotAuth.getShard()), puuid, "{}", riotAuth.getClientPlatform(),
				riotAuth.getClientVersion(), riotAuth.getEntitlementsToken(), riotAuth.getAuthorization());
	}

	@Override
	public DailyStore getDailyStores(String puuid, String accessToken, String entitlementsToken, String region) {

		StorefrontResponse storefrontResponse = getStorefront(puuid, accessToken, entitlementsToken, region);
		List<DailyStore.DailyStoreItem> dailyStoreItems = new java.util.ArrayList<>();

		// Fetch all skins once to find parent skins for the levels
		WeaponSkinResponse allSkins = valorantMetadataService.getAllWeaponSkins();

		// Fetch all content tiers
		ContentTierResponse contentTiers = valorantMetadataService
				.getContentTiers();

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
							: null);

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

}
