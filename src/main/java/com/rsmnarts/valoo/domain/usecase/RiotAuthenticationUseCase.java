package com.rsmnarts.valoo.domain.usecase;

import com.rsmnarts.valoo.domain.model.AuthenticatedUser;

public interface RiotAuthenticationUseCase {

	String getLoginUrl();

	AuthenticatedUser processRedirectUrl(String url);
}
