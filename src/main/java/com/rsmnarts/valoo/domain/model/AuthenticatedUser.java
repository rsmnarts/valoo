package com.rsmnarts.valoo.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticatedUser {
	private String accessToken;
	private String entitlementsToken;
	private String userId;
}
