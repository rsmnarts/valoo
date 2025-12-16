package com.rsmnarts.valoo.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data

public class CurrencyResponse {
	@JsonProperty("uuid")
	private String uuid;
	@JsonProperty("displayName")
	private String displayName;
	@JsonProperty("displayIcon")
	private String displayIcon;
	@JsonProperty("rank")
	private int rank;
	@JsonProperty("rewardPreviewIcon")
	private String rewardPreviewIcon;
	@JsonProperty("assetPath")
	private String assetPath;
}
