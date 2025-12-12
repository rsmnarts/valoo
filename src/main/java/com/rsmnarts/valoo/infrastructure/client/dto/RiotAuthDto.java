package com.rsmnarts.valoo.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

public class RiotAuthDto {

	@Data
	@Builder
	public static class AuthRequest {
		@JsonProperty("acr_values")
		private String acrValues;

		@JsonProperty("claims")
		private String claims;

		@JsonProperty("client_id")
		private String clientId;

		@JsonProperty("code_challenge")
		private String codeChallenge;

		@JsonProperty("code_challenge_method")
		private String codeChallengeMethod;

		@JsonProperty("nonce")
		private String nonce;

		@JsonProperty("redirect_uri")
		private String redirectUri;

		@JsonProperty("response_type")
		private String responseType;

		@JsonProperty("scope")
		private String scope;
	}

	@Data
	@Builder
	public static class LoginRequest {
		@JsonProperty("language")
		private String language;

		@JsonProperty("password")
		private String password;

		@JsonProperty("region")
		private String region;

		@JsonProperty("remember")
		private boolean remember;

		@JsonProperty("type")
		private String type;

		@JsonProperty("username")
		private String username;
	}

	@Data
	public static class AuthResponse {
		@JsonProperty("type")
		private String type;

		@JsonProperty("response")
		private ResponseData response;

		@JsonProperty("country")
		private String country;

		@JsonProperty("error")
		private String error;
	}

	@Data
	public static class ResponseData {
		@JsonProperty("mode")
		private String mode;

		@JsonProperty("parameters")
		private Parameters parameters;
	}

	@Data
	public static class Parameters {
		@JsonProperty("uri")
		private String uri;
	}

	@Data
	public static class EntitlementsResponse {
		@JsonProperty("entitlements_token")
		private String entitlementsToken;
	}

	@Data
	public static class UserInfoResponse {
		@JsonProperty("sub")
		private String sub;
	}
}
