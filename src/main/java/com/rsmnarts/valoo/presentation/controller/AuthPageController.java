package com.rsmnarts.valoo.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.rsmnarts.valoo.domain.usecase.RiotAuthenticationUseCase;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class AuthPageController {

	private final RiotAuthenticationUseCase riotAuthenticationUseCase;

	@GetMapping()
	public String loginPage(Model model) {
		model.addAttribute("loginUrl", riotAuthenticationUseCase.getLoginUrl());
		return "login";
	}
}
