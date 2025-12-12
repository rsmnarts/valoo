package com.rsmnarts.valoo.infrastructure.client.config;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rsmnarts.valoo.common.utils.json;
import com.rsmnarts.valoo.infrastructure.client.dto.error.RiotApiError;

import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RiotValorantApiErrorDecoder implements ErrorDecoder {

	private final ErrorDecoder defaultDecoder = new Default();

	@Override
	public Exception decode(String methodKey, Response response) {
		if (response.status() == 400 && response.body() != null) {
			String bodyStr = null;
			try {
				// Read the body
				bodyStr = Util.toString(response.body().asReader(StandardCharsets.UTF_8));

				if (bodyStr.trim().startsWith("[")) {
					// Try to map to our error structure (List)
					List<RiotApiError> errors = json.Unmarshal(bodyStr, new TypeReference<List<RiotApiError>>() {
					});

					if (errors != null && !errors.isEmpty()) {
						RiotApiError error = errors.get(0);
						// Check for specific error code and message
						if ("BAD_CLAIMS".equals(error.getErrorCode())
								|| "Failure validating/decoding RSO Access Token".equals(error.getMessage())) {
							return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
									"Riot API Unauthorized: " + error.getMessage());
						}
					}
				} else if (bodyStr.trim().startsWith("{")) {
					// Try to map to our error structure (Single Object)
					RiotApiError error = json.Unmarshal(bodyStr, RiotApiError.class);
					if (error != null) {
						if ("BAD_CLAIMS".equals(error.getErrorCode())
								|| "Failure validating/decoding RSO Access Token".equals(error.getMessage())) {
							return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
									"Riot API Unauthorized: " + error.getMessage());
						}
					}
				}
			} catch (Exception e) {
				// Parsing failed or check failed, proceed to default
				log.error("Failed to parse Riot API error response", e);
			}

			// If we read the body, we need to provide it back to the default decoder
			if (bodyStr != null) {
				response = response.toBuilder().body(bodyStr, StandardCharsets.UTF_8).build();
			}
		}

		return defaultDecoder.decode(methodKey, response);
	}
}
