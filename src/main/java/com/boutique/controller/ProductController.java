package com.boutique.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.boutique.entity.Order;
import com.boutique.entity.OrderItem;
import com.boutique.entity.Product;
import com.boutique.repository.OrderRepository;
import com.boutique.repository.ProductRepository;

/** Reading products is open to everyone. Adding and deleting lives in AdminController. */
@RestController
@RequestMapping("/api/products")
public class ProductController {

	private static final int DEFAULT_LIMIT = 4;

	private final ProductRepository repo;
	private final OrderRepository orders;

	ProductController(ProductRepository repo, OrderRepository orders) {
		this.repo = repo;
		this.orders = orders;
	}

	@GetMapping
	public List<Product> all(@RequestParam(required = false) String category) {
		if (category == null || category.isBlank()) {
			return repo.findAllByOrderByIdAsc();
		}
		return repo.findByCategoryOrderByIdAsc(category);
	}

	/** Every category in the shop with how many products it holds. */
	@GetMapping("/categories")
	public List<Map<String, Object>> categories() {
		Map<String, Integer> counts = new HashMap<>();

		for (Product product : repo.findAllByOrderByIdAsc()) {
			String name = product.getCategory();
			if (name == null || name.isBlank()) continue;
			counts.put(name, counts.getOrDefault(name, 0) + 1);
		}

		List<String> names = new ArrayList<>(counts.keySet());
		names.sort(String::compareTo);

		List<Map<String, Object>> list = new ArrayList<>();
		for (String name : names) {
			list.add(Map.of("name", name, "count", counts.get(name)));
		}
		return list;
	}

	/** Newest arrivals for the home page. */
	@GetMapping("/new")
	public List<Product> newArrivals() {
		return firstFew(repo.findByNewArrivalTrueOrderByIdDesc());
	}

	/** Best sellers, counted from the items in every order. */
	@GetMapping("/popular")
	public List<Product> popular() {
		Map<Long, Integer> sold = new HashMap<>();

		for (Order order : orders.findAll()) {
			for (OrderItem item : order.getItems()) {
				sold.put(item.getProductId(), sold.getOrDefault(item.getProductId(), 0) + item.getQty());
			}
		}

		List<Product> list = new ArrayList<>(repo.findAllByOrderByIdAsc());
		list.sort((a, b) -> sold.getOrDefault(b.getId(), 0) - sold.getOrDefault(a.getId(), 0));
		return firstFew(list);
	}

	@GetMapping("/{id}")
	public Product one(@PathVariable Long id) {
		return repo.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
	}

	/** Other products from the same category, for "You may also like". */
	@GetMapping("/{id}/related")
	public List<Product> related(@PathVariable Long id) {
		Product product = one(id);

		List<Product> list = new ArrayList<>();
		for (Product other : repo.findByCategoryOrderByIdAsc(product.getCategory())) {
			if (!other.getId().equals(id)) list.add(other);
		}
		return firstFew(list);
	}

	/** Pictures uploaded by the admin are kept in the database. */
	@GetMapping("/{id}/image")
	public ResponseEntity<byte[]> image(@PathVariable Long id) {
		Product product = one(id);
		if (product.getImageData() == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No picture for this product");
		}
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(product.getImageType()))
				.body(product.getImageData());
	}

	/** The home page sections only show a handful of products. */
	private List<Product> firstFew(List<Product> list) {
		if (list.size() <= DEFAULT_LIMIT) return list;
		return list.subList(0, DEFAULT_LIMIT);
	}
}
