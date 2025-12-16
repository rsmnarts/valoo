package com.rsmnarts.valoo.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.rsmnarts.valoo.infrastructure.client.dto.ContentTierResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.DailyStoreLevelResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.VersionResponse;
import com.rsmnarts.valoo.infrastructure.client.dto.WeaponSkinResponse;

import feign.Headers;

@FeignClient(name = "valorant-api.com", url = "https://valorant-api.com")
@Headers({ "Connection: close", "language: en-US" })
public interface ValorantApiComClient {

	@GetMapping("/v1/version")
	VersionResponse getVersion();

	@GetMapping("/v1/bundles")
	Object getBundles();

	@GetMapping("/v1/weapons/skinlevels/{dailyStoreLevelUuid}")
	DailyStoreLevelResponse getWeaponsSkins(@PathVariable String dailyStoreLevelUuid);

	@GetMapping("/v1/weapons/skins")
	WeaponSkinResponse getAllWeaponSkins();

	@GetMapping("/v1/contenttiers")
	ContentTierResponse getContentTiers();
}
