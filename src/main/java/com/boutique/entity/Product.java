package com.boutique.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "products")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	private String category;

	private int price;

	private int stock;

	/** Where the picture is shown from: an outside link, or our own image endpoint. */
	@Column(length = 500)
	private String image;

	/** Uploaded picture. Left out of the JSON, served by GET /api/products/{id}/image. */
	@JsonIgnore
	@Column(columnDefinition = "bytea")
	private byte[] imageData;

	@JsonIgnore
	private String imageType;

	private String colour;

	/** Sizes kept as one text value, for example "S,M,L". */
	private String sizes;

	private boolean newArrival;
}
