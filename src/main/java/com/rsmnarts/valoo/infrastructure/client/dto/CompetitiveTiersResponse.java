package com.rsmnarts.valoo.infrastructure.client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CompetitiveTiersResponse {
	@JsonProperty("status")
	private int status;
	@JsonProperty("data")
	private List<TierSet> data;

	@Data
	public static class TierSet {
		@JsonProperty("uuid")
		private String uuid;
		@JsonProperty("tiers")
		private List<Tier> tiers;
	}

	@Data
	public static class Tier {
		@JsonProperty("tier")
		private int tier;
		@JsonProperty("tierName")
		private String tierName;
		@JsonProperty("smallIcon")
		private String smallIcon;
		@JsonProperty("largeIcon")
		private String largeIcon;
		@JsonProperty("color")
		private String color;
	}
}
