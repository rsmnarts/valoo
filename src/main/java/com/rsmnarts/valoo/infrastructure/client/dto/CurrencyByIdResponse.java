package com.rsmnarts.valoo.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CurrencyByIdResponse {
	@JsonProperty("status")
	private int status;
	@JsonProperty("data")
	private CurrencyResponse data;
}
