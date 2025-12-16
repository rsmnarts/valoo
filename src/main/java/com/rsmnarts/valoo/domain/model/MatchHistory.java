package com.rsmnarts.valoo.domain.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchHistory {
	private List<Match> matches;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Match {
		private String matchId;
		private String mapName;
		private String mapBg;
		private Long gameStartTime;
		private String agentName;
		private String agentIcon;
		private String score; // "13 - 10"
		private String kda; // "20 / 10 / 5"
		private String result; // "VICTORY", "DEFEAT"
		private String rankName;
		private String rankIcon;
		private boolean isRanked;
		private String queueId;
	}
}
