package com.boutique.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/** Makes the sign-in token and reads the email back out of it. */
@Component
public class JwtUtil {

	private final Key key;
	private final long expiration;

	public JwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expiration) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes());
		this.expiration = expiration;
	}

	public String generateToken(String email) {
		return Jwts.builder()
				.setSubject(email)
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + expiration))
				.signWith(key)
				.compact();
	}

	/** Throws if the token is fake or expired. */
	public String readEmail(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
	}
}
