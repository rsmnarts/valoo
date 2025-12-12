package com.rsmnarts.valoo.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerNameResponse {
	@JsonProperty("DisplayName")
	private String displayName;

	@JsonProperty("Subject")
	private String subject;

	@JsonProperty("GameName")
	private String gameName;

	@JsonProperty("TagLine")
	private String tagLine;
}
