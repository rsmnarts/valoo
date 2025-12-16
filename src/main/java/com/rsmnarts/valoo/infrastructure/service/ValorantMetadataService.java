package com.rsmnarts.valoo.infrastructure.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.rsmnarts.valoo.infrastructure.client.ValorantApiComClient;
import com.rsmnarts.valoo.infrastructure.client.dto.AgentsResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.CompetitiveTiersResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.ContentTierResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.CurrencyByIdResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.DailyStoreLevelResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.MapsResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.VersionResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.WeaponSkinResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValorantMetadataService {

	private final ValorantApiComClient valorantApiComClient;

	@Cacheable("valorant-version")
	public VersionResponse getVersion() {
		return valorantApiComClient.getVersion();
	}

	@Cacheable(value = "weapon-skin-details", key = "#uuid")
	public DailyStoreLevelResponse getDailyStore(String uuid) {
		return valorantApiComClient.getWeaponsSkins(uuid);
	}

	@Cacheable(value = "all-daily-store")
	public WeaponSkinResponse getAllWeaponSkins() {
		return valorantApiComClient.getAllWeaponSkins();
	}

	@Cacheable(value = "content-tiers")
	public ContentTierResponse getContentTiers() {
		return valorantApiComClient.getContentTiers();
	}

	@Cacheable(value = "agents")
	public AgentsResponse getAgents() {
		return valorantApiComClient.getAgents();
	}

	@Cacheable(value = "maps")
	public MapsResponse getMaps() {
		return valorantApiComClient.getMaps();
	}

	@Cacheable(value = "competitive-tiers")
	public CompetitiveTiersResponse getCompetitiveTiers() {
		return valorantApiComClient.getCompetitiveTiers();
	}

	@Cacheable(value = "currency")
	public CurrencyByIdResponse getCurrency(String id) {
		return valorantApiComClient.getCurrency(id);
	}
}
