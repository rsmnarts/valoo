package com.rsmnarts.valoo.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class DailyStoreLevelResponse {

	@JsonProperty("status")
	private int status;

	@JsonProperty("data")
	private DailyStoreLevelData data;

	@Data
	public static class DailyStoreLevelData {
		@JsonProperty("uuid")
		private String uuid;
		@JsonProperty("displayName")
		private String displayName;
		@JsonProperty("levelItem")
		private String levelItem;
		@JsonProperty("displayIcon")
		private String displayIcon;
		@JsonProperty("streamedVideo")
		private String streamedVideo;
		@JsonProperty("assetPath")
		private String assetPath;
	}
}
