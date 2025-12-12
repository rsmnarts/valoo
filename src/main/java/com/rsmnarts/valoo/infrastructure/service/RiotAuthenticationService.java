package com.rsmnarts.valoo.infrastructure.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.rsmnarts.valoo.domain.model.AuthenticatedUser;
import com.rsmnarts.valoo.domain.usecase.RiotAuthenticationUseCase;
import com.rsmnarts.valoo.infrastructure.client.AuthRiotApiClient;
import com.rsmnarts.valoo.infrastructure.client.EntitlementsApiClient;
import com.rsmnarts.valoo.infrastructure.client.dto.RiotAuthDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiotAuthenticationService implements RiotAuthenticationUseCase {

	private final AuthRiotApiClient authRiotApiClient;
	private final EntitlementsApiClient entitlementsApiClient;

	@Override
	public String getLoginUrl() {
		String baseUrl = "https://auth.riotgames.com/authorize";
		String redirectUri = "https://playvalorant.com/opt_in";
		String clientId = "play-valorant-web-prod";
		String responseType = "token%20id_token";
		String nonce = "1";

		return baseUrl + "?redirect_uri=" + redirectUri + "&client_id=" + clientId + "&response_type=" + responseType
				+ "&nonce=" + nonce;
	}

	@Override
	public AuthenticatedUser processRedirectUrl(String url) {
		String accessToken = extractAccessToken(url);

		// 3. Get Entitlements
		String authorizationHeader = "Bearer " + accessToken;
		RiotAuthDto.EntitlementsResponse entitlementsResponse = entitlementsApiClient.getEntitlements(authorizationHeader,
				"{}");

		// 4. Get User ID from UserInfo
		RiotAuthDto.UserInfoResponse userInfoResponse = authRiotApiClient.getUserInfo(authorizationHeader);
		String userId = userInfoResponse.getSub();

		return AuthenticatedUser.builder()
				.accessToken(accessToken)
				.entitlementsToken(entitlementsResponse.getEntitlementsToken())
				.userId(userId)
				.build();
	}

	private String extractAccessToken(String uriString) {
		Pattern pattern = Pattern
				.compile("access_token=((?:[a-zA-Z]|\\d|\\.|-|_)*).*id_token=((?:[a-zA-Z]|\\d|\\.|-|_)*).*expires_in=(\\d*)");
		Matcher matcher = pattern.matcher(uriString);
		if (matcher.find()) {
			return matcher.group(1);
		}
		throw new RuntimeException("Access token not found in URI");
	}
}
