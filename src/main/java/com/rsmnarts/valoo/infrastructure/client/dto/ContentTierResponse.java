package com.rsmnarts.valoo.infrastructure.client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ContentTierResponse {
	@JsonProperty("status")
	private int status;
	@JsonProperty("data")
	private List<ContentTier> data;

	@Data
	public static class ContentTier {
		@JsonProperty("uuid")
		private String uuid;
		@JsonProperty("displayName")
		private String displayName;
		@JsonProperty("displayIcon")
		private String displayIcon;
		@JsonProperty("rank")
		private int rank;
		@JsonProperty("juiceValue")
		private int juiceValue;
		@JsonProperty("juiceCost")
		private int juiceCost;
		@JsonProperty("highlightColor")
		private String highlightColor;
	}
}
