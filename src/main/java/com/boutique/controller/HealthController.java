package com.boutique.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Railway calls this before it sends traffic to the app. */
@RestController
public class HealthController {

	@GetMapping("/api/health")
	public Map<String, String> health() {
		return Map.of("status", "UP");
	}
}
