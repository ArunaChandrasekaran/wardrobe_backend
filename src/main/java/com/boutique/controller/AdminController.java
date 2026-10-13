package com.boutique.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.boutique.entity.Order;
import com.boutique.entity.Product;
import com.boutique.entity.User;
import com.boutique.repository.OrderRepository;
import com.boutique.repository.ProductRepository;
import com.boutique.service.UserService;

/** Everything under /api/admin needs the ADMIN role. See SecurityConfig. */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

	private static final List<String> STATUSES = List.of(
			"Confirmed", "Preparing", "Ready", "Out for Delivery", "Delivered", "Cancelled");

	private final UserService users;
	private final ProductRepository products;
	private final OrderRepository orders;

	AdminController(UserService users, ProductRepository products, OrderRepository orders) {
		this.users = users;
		this.products = products;
		this.orders = orders;
	}

	/** The statuses the admin can pick, so the dropdown is not hardcoded in the page. */
	@GetMapping("/order-statuses")
	public List<String> orderStatuses() {
		return STATUSES;
	}

	@GetMapping("/customers")
	public List<User> customers() {
		return users.listCustomers();
	}

	@PostMapping("/products")
	public Product addProduct(@RequestBody Product product) {
		if (product.getName() == null || product.getName().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name is required");
		}
		if (product.getPrice() <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price must be more than 0");
		}
		product.setId(null);
		return products.save(product);
	}

	/** Saves the picture bytes in the database and points the product at our image endpoint. */
	@PostMapping("/products/{id}/image")
	public Product uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
		String type = file.getContentType();
		if (file.isEmpty() || type == null || !type.startsWith("image/")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pick an image file");
		}
		Product product = products.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
		try {
			product.setImageData(file.getBytes());
		} catch (IOException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read the picture");
		}
		product.setImageType(type);
		product.setImage(ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/api/products/" + id + "/image")
				.toUriString());
		return products.save(product);
	}

	@DeleteMapping("/products/{id}")
	public Map<String, String> deleteProduct(@PathVariable Long id) {
		if (!products.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
		}
		products.deleteById(id);
		return Map.of("message", "Product deleted");
	}

	@GetMapping("/orders")
	public List<Order> allOrders() {
		return orders.findAllByOrderByIdAsc();
	}

	@PutMapping("/orders/{id}/status")
	public Order updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
		String status = body.get("status");
		if (!STATUSES.contains(status)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown status");
		}
		Order order = orders.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
		order.setStatus(status);
		return orders.save(order);
	}
}
