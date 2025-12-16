package com.rsmnarts.valoo.infrastructure.client.dto;

import java.util.List;

import lombok.Data;

@Data
public class CurrenciesResponse {
	private int status;
	private List<CurrencyResponse> data;
}
