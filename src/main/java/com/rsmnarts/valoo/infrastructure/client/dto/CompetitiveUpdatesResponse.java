package com.rsmnarts.valoo.infrastructure.client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompetitiveUpdatesResponse {
	@JsonProperty("Version")
	private long version;
	@JsonProperty("Subject")
	private String subject;
	@JsonProperty("Matches")
	private List<CompetitiveUpdate> matches;

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class CompetitiveUpdate {
		@JsonProperty("MatchID")
		private String matchId;
		@JsonProperty("MapID")
		private String mapId;
		@JsonProperty("MatchStartTime")
		private long matchStartTime;
		@JsonProperty("TierAfterUpdate")
		private int tierAfterUpdate;
		@JsonProperty("TierBeforeUpdate")
		private int tierBeforeUpdate;
		@JsonProperty("RankedRatingAfterUpdate")
		private int rankedRatingAfterUpdate;
		@JsonProperty("RankedRatingBeforeUpdate")
		private int rankedRatingBeforeUpdate;
		@JsonProperty("RankedRatingChange")
		private int rankedRatingChange;
		@JsonProperty("RankedRatingEarned")
		private int rankedRatingEarned; // Sometimes this is more reliable
		@JsonProperty("CompetitiveTier")
		private int competitiveTier;
	}
}
