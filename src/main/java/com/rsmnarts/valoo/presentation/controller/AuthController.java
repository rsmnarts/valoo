package com.rsmnarts.valoo.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rsmnarts.valoo.domain.model.AuthenticatedUser;
import com.rsmnarts.valoo.domain.usecase.RiotAuthenticationUseCase;
import com.rsmnarts.valoo.presentation.dto.CallbackRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final RiotAuthenticationUseCase riotAuthenticationUseCase;

	@PostMapping("/process-callback")
	public ResponseEntity<AuthenticatedUser> processCallback(@RequestBody CallbackRequest request) {
		return ResponseEntity.ok(riotAuthenticationUseCase.processRedirectUrl(request.getUrl()));
	}
}
