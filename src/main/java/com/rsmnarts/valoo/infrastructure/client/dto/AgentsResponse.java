package com.rsmnarts.valoo.infrastructure.client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AgentsResponse {
	@JsonProperty("status")
	private int status;
	@JsonProperty("data")
	private List<Agent> data;

	@Data
	public static class Agent {
		@JsonProperty("uuid")
		private String uuid;
		@JsonProperty("displayName")
		private String displayName;
		@JsonProperty("displayIcon")
		private String displayIcon;
		@JsonProperty("role")
		private Role role;
	}

	@Data
	public static class Role {
		@JsonProperty("uuid")
		private String uuid;
		@JsonProperty("displayName")
		private String displayName;
	}
}
