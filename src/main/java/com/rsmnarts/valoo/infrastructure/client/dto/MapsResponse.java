package com.rsmnarts.valoo.infrastructure.client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class MapsResponse {
	@JsonProperty("status")
	private int status;
	@JsonProperty("data")
	private List<ValorantMap> data;

	@Data
	public static class ValorantMap {
		@JsonProperty("uuid")
		private String uuid;
		@JsonProperty("displayName")
		private String displayName;
		@JsonProperty("mapUrl")
		private String mapUrl; // Often this is the internal name, but maybe stick to displayName or uuid.
		// Actually api returns 'mapUrl' as like '/Game/Maps/...' which helps matching
		// with riot response.
		@JsonProperty("stylizedBackgroundImage")
		private String stylizedBackgroundImage;
		@JsonProperty("premierBackgroundImage")
		private String premierBackgroundImage;
		@JsonProperty("splash")
		private String splash;
	}
}
