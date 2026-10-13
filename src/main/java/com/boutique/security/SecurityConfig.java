package com.boutique.security;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/** Decides which URLs are open to everyone and which ones need a sign-in. */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {

		// No session and no HTML forms, so CSRF protection is not needed here.
		http.csrf(csrf -> csrf.disable());
		http.cors(Customizer.withDefaults());
		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		http.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/register", "/api/auth/login").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()
				.requestMatchers("/api/admin/**").hasRole("ADMIN")
				.anyRequest().authenticated());

		// Send back the same {"error": "..."} shape the rest of the API uses.
		http.exceptionHandling(ex -> ex
				.authenticationEntryPoint((req, res, e) -> writeError(res, 401, "Please sign in"))
				.accessDeniedHandler((req, res, e) -> writeError(res, 403, "Admins only")));

		http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	private void writeError(HttpServletResponse res, int status, String message) throws IOException {
		res.setStatus(status);
		res.setContentType("application/json");
		res.getWriter().write("{\"error\":\"" + message + "\"}");
	}

	/** The pages are served by Live Server on another port, so that port must be allowed. */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration cors = new CorsConfiguration();
		cors.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
		cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		cors.setAllowedHeaders(List.of("*"));
		cors.setAllowPrivateNetwork(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", cors);
		return source;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}
