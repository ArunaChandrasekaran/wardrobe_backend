package com.boutique.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boutique.entity.User;
import com.boutique.security.JwtUtil;
import com.boutique.service.UserService;

/** Register, sign in, and read or edit the signed-in account. */
@RestController
@RequestMapping("/api")
public class AuthController {

	private final UserService service;
	private final AuthenticationManager authManager;
	private final JwtUtil jwtUtil;

	AuthController(AuthenticationManager authManager, JwtUtil jwtUtil, UserService service) {
		this.jwtUtil = jwtUtil;
		this.authManager = authManager;
		this.service = service;
	}

	@PostMapping("/register")
	public Map<String, String> register(@RequestBody User user) {
		service.register(user);
		return Map.of("message", "User registered");
	}

	@PostMapping("/auth/login")
	public ResponseEntity<Map<String, String>> login(@RequestBody User body) {
		String email = UserService.cleanEmail(body.getEmail());

		try {
			authManager.authenticate(new UsernamePasswordAuthenticationToken(email, body.getPassword()));
		} catch (Exception e) {
			return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
		}

		return ResponseEntity.ok(Map.of("token", jwtUtil.generateToken(email)));
	}

	@GetMapping("/auth/me")
	public Map<String, Object> me(Authentication auth) {
		return details(service.findByEmail(auth.getName()));
	}

	@PutMapping("/auth/me")
	public Map<String, Object> updateMe(Authentication auth, @RequestBody User body) {
		return details(service.updateProfile(auth.getName(), body.getName(), body.getPhone()));
	}

	/** The account fields the pages need. The password is never included. */
	private Map<String, Object> details(User user) {
		return Map.of(
				"id", user.getId(),
				"name", user.getName() == null ? "" : user.getName(),
				"email", user.getEmail(),
				"phone", user.getPhone() == null ? "" : user.getPhone(),
				"role", user.getRole());
	}
}
