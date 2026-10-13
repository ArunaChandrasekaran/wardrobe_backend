package com.boutique.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.boutique.entity.User;
import com.boutique.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

	private final UserRepository repo;
	private final PasswordEncoder encoder;

	UserService(PasswordEncoder encoder, UserRepository repo) {
		this.encoder = encoder;
		this.repo = repo;
	}

	/** Emails are always stored in lower case, so sign-in is not case sensitive. */
	public static String cleanEmail(String email) {
		return email == null ? "" : email.trim().toLowerCase();
	}

	public User register(User user) {
		String email = cleanEmail(user.getEmail());

		if (email.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
		}
		if (user.getPassword() == null || user.getPassword().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
		}
		if (repo.existsByEmail(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
		}

		user.setEmail(email);
		user.setPassword(encoder.encode(user.getPassword()));
		user.setRole("USER");
		return repo.save(user);
	}

	public User findByEmail(String email) {
		return repo.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
	}

	public List<User> listCustomers() {
		return repo.findByRoleOrderByIdDesc("USER");
	}

	public User updateProfile(String email, String name, String phone) {
		User user = findByEmail(email);
		if (name != null && !name.isBlank()) user.setName(name);
		if (phone != null) user.setPhone(phone);
		return repo.save(user);
	}

	/** Spring Security calls this to load the account it should check the password against. */
	@Override
	public UserDetails loadUserByUsername(String email) {
		User user = findByEmail(email);
		return org.springframework.security.core.userdetails.User
				.withUsername(user.getEmail())
				.password(user.getPassword())
				.roles(user.getRole())
				.build();
	}
}
