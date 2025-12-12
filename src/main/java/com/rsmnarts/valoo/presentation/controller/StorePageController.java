package com.rsmnarts.valoo.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/stores")
public class StorePageController {

	@GetMapping()
	public String storePage() {
		return "daily-store";
	}

	@GetMapping("/night-market")
	public String nightMarketPage() {
		return "night-market";
	}
}
