package com.rsmnarts.valoo.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientPlatformRequest {
	@JsonProperty("platformType")
	private String platformType;

	@JsonProperty("platformOS")
	private String platformOS;

	@JsonProperty("platformOSVersion")
	private String platformOSVersion;

	@JsonProperty("platformChipset")
	private String platformChipset;
}
