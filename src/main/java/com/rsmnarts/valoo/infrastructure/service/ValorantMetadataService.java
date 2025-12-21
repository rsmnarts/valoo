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

	@Cacheable(value = "valorant-version", unless = "#result == null")
	public VersionResponse getVersion() {
		return valorantApiComClient.getVersion();
	}

	@Cacheable(value = "weapon-skin-details", key = "#uuid", unless = "#result == null")
	public DailyStoreLevelResponse getDailyStore(String uuid) {
		return valorantApiComClient.getWeaponsSkins(uuid);
	}

	@Cacheable(value = "all-daily-store", unless = "#result == null")
	public WeaponSkinResponse getAllWeaponSkins() {
		return valorantApiComClient.getAllWeaponSkins();
	}

	@Cacheable(value = "content-tiers", unless = "#result == null")
	public ContentTierResponse getContentTiers() {
		return valorantApiComClient.getContentTiers();
	}

	@Cacheable(value = "agents", unless = "#result == null")
	public AgentsResponse getAgents() {
		return valorantApiComClient.getAgents();
	}

	@Cacheable(value = "maps", unless = "#result == null")
	public MapsResponse getMaps() {
		return valorantApiComClient.getMaps();
	}

	@Cacheable(value = "competitive-tiers", unless = "#result == null")
	public CompetitiveTiersResponse getCompetitiveTiers() {
		return valorantApiComClient.getCompetitiveTiers();
	}

	@Cacheable(value = "currency", unless = "#result == null")
	public CurrencyByIdResponse getCurrency(String id) {
		return valorantApiComClient.getCurrency(id);
	}
}
