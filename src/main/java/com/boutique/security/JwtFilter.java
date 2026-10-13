package com.boutique.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.boutique.service.UserService;

/** Runs on every request: reads the token and tells Spring who is signed in. */
@Component
public class JwtFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final UserService userService;

	public JwtFilter(JwtUtil jwtUtil, UserService userService) {
		this.jwtUtil = jwtUtil;
		this.userService = userService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws ServletException, IOException {

		String header = req.getHeader("Authorization");

		if (header != null && header.startsWith("Bearer ")) {
			try {
				String email = jwtUtil.readEmail(header.substring(7));
				UserDetails user = userService.loadUserByUsername(email);
				SecurityContextHolder.getContext().setAuthentication(
						new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
			} catch (Exception e) {
				// A bad or expired token simply means nobody is signed in.
				SecurityContextHolder.clearContext();
			}
		}

		chain.doFilter(req, res);
	}
}
