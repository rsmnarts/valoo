package com.rsmnarts.valoo.infrastructure.client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchDetailsResponse {
	@JsonProperty("matchInfo")
	private MatchInfo matchInfo;
	@JsonProperty("players")
	private List<Player> players;
	@JsonProperty("teams")
	private List<Team> teams;

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class MatchInfo {
		@JsonProperty("matchId")
		private String matchId;
		@JsonProperty("mapId")
		private String mapId;
		@JsonProperty("gameLengthMillis")
		private Long gameLengthMillis;
		@JsonProperty("gameStartMillis")
		private Long gameStartMillis;
		@JsonProperty("provisioningFlowId")
		private String provisioningFlowId;
		@JsonProperty("gameMode")
		private String gameMode;
		@JsonProperty("isRanked")
		private Boolean isRanked;
		@JsonProperty("queueID")
		private String queueID;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Player {
		@JsonProperty("subject")
		private String subject;
		@JsonProperty("gameName")
		private String gameName;
		@JsonProperty("tagLine")
		private String tagLine;
		@JsonProperty("teamId")
		private String teamId;
		@JsonProperty("partyId")
		private String partyId;
		@JsonProperty("characterId")
		private String characterId;
		@JsonProperty("stats")
		private Stats stats;
		@JsonProperty("competitiveTier")
		private int competitiveTier;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Stats {
		@JsonProperty("score")
		private int score;
		@JsonProperty("roundsPlayed")
		private int roundsPlayed;
		@JsonProperty("kills")
		private int kills;
		@JsonProperty("deaths")
		private int deaths;
		@JsonProperty("assists")
		private int assists;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Team {
		@JsonProperty("teamId")
		private String teamId;
		@JsonProperty("won")
		private Boolean won;
		@JsonProperty("roundsPlayed")
		private int roundsPlayed;
		@JsonProperty("roundsWon")
		private int roundsWon;
		@JsonProperty("numPoints")
		private int numPoints;
	}
}
