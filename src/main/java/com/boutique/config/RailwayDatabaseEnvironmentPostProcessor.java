package com.boutique.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Railway gives DATABASE_URL as postgres://user:pass@host:port/db.
 * Spring needs a JDBC url plus a separate user and password.
 */
public class RailwayDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		String raw = environment.getProperty("DATABASE_URL");
		if (raw == null || raw.isBlank()) {
			return;
		}

		Map<String, Object> mapped = new HashMap<>();
		if (raw.startsWith("jdbc:")) {
			mapped.put("spring.datasource.url", raw);
		} else {
			applyPostgresUrl(raw, mapped);
		}
		environment.getPropertySources().addFirst(new MapPropertySource("railwayDatabaseUrl", mapped));
	}

	private static void applyPostgresUrl(String raw, Map<String, Object> mapped) {
		String normalized = raw.startsWith("postgres://")
				? "postgresql://" + raw.substring("postgres://".length())
				: raw;
		if (!normalized.startsWith("postgresql://")) {
			throw new IllegalStateException("DATABASE_URL must be a postgres or jdbc URL");
		}

		URI uri;
		try {
			uri = URI.create(normalized);
		} catch (IllegalArgumentException ex) {
			throw new IllegalStateException("DATABASE_URL is not a valid postgres URL");
		}
		if (uri.getHost() == null) {
			throw new IllegalStateException("DATABASE_URL is missing a host");
		}

		String username = "";
		String password = "";
		String userInfo = uri.getUserInfo();
		if (userInfo != null) {
			int colon = userInfo.indexOf(':');
			if (colon >= 0) {
				username = decode(userInfo.substring(0, colon));
				password = decode(userInfo.substring(colon + 1));
			} else {
				username = decode(userInfo);
			}
		}

		String database = uri.getPath() == null ? "" : uri.getPath();
		if (database.startsWith("/")) {
			database = database.substring(1);
		}
		int port = uri.getPort() == -1 ? 5432 : uri.getPort();

		StringBuilder jdbc = new StringBuilder("jdbc:postgresql://")
				.append(uri.getHost())
				.append(':')
				.append(port)
				.append('/')
				.append(database);
		if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
			jdbc.append('?').append(uri.getQuery());
		}

		mapped.put("spring.datasource.url", jdbc.toString());
		if (!username.isEmpty()) {
			mapped.put("spring.datasource.username", username);
		}
		if (!password.isEmpty()) {
			mapped.put("spring.datasource.password", password);
		}
	}

	private static String decode(String value) {
		return URLDecoder.decode(value.replace("+", "%2B"), StandardCharsets.UTF_8);
	}
}
