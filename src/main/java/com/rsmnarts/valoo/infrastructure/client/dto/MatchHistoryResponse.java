package com.rsmnarts.valoo.infrastructure.client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class MatchHistoryResponse {
	@JsonProperty("Subject")
	private String subject;
	@JsonProperty("BeginIndex")
	private int beginIndex;
	@JsonProperty("EndIndex")
	private int endIndex;
	@JsonProperty("Total")
	private int total;
	@JsonProperty("History")
	private List<History> history;

	@Data
	public static class History {
		@JsonProperty("MatchID")
		private String matchID;
		@JsonProperty("GameStartTime")
		private Long gameStartTime;
		@JsonProperty("QueueID")
		private String queueID;
	}
}
