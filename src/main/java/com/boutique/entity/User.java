package com.boutique.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@Column(unique = true, nullable = false)
	private String email;

	private String phone;

	/** Stored hashed. WRITE_ONLY keeps it out of every JSON response. */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;

	/** "USER" or "ADMIN". */
	private String role;
}
