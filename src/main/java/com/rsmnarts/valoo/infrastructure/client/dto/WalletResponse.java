package com.rsmnarts.valoo.infrastructure.client.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class WalletResponse {
	@JsonProperty("Balances")
	private Map<String, Integer> balances;
}
