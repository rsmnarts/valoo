package com.rsmnarts.valoo.infrastructure.client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class WeaponSkinResponse {
	@JsonProperty("status")
	private int status;
	@JsonProperty("data")
	private List<WeaponSkin> data;

	@Data
	public static class WeaponSkin {
		@JsonProperty("uuid")
		private String uuid;
		@JsonProperty("displayName")
		private String displayName;
		@JsonProperty("displayIcon")
		private String displayIcon;
		@JsonProperty("contentTierUuid")
		private String contentTierUuid;
		@JsonProperty("levels")
		private List<WeaponSkinLevel> levels;
		@JsonProperty("chromas")
		private List<WeaponSkinChroma> chromas;
	}

	@Data
	public static class WeaponSkinLevel {
		@JsonProperty("uuid")
		private String uuid;
		@JsonProperty("displayName")
		private String displayName;
		@JsonProperty("displayIcon")
		private String displayIcon;
		@JsonProperty("streamedVideo")
		private String streamedVideo;
	}

	@Data
	public static class WeaponSkinChroma {
		@JsonProperty("uuid")
		private String uuid;
		@JsonProperty("displayName")
		private String displayName;
		@JsonProperty("displayIcon")
		private String displayIcon;
		@JsonProperty("swatch")
		private String swatch;
		@JsonProperty("streamedVideo")
		private String streamedVideo;
	}
}
