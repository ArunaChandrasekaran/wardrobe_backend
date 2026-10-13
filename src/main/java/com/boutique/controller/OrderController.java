package com.boutique.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boutique.entity.Order;
import com.boutique.entity.User;
import com.boutique.service.OrderService;
import com.boutique.service.UserService;

/** Orders of the signed-in customer. The admin sees all orders in AdminController. */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orders;
	private final UserService users;

	OrderController(OrderService orders, UserService users) {
		this.orders = orders;
		this.users = users;
	}

	// @GetMapping
	// public List<Order> myOrders(Authentication auth) {
	// 	return orders.listFor(currentUser(auth));
	// }

	@GetMapping("/{id}")
	public Order oneOrder(@PathVariable Long id, Authentication auth) {
		return orders.findOne(id, currentUser(auth));
	}

	@PostMapping
	public Order placeOrder(@RequestBody Map<String, Object> body, Authentication auth) {
		return orders.place(currentUser(auth), body);
	}

	/** auth.getName() is the email kept inside the sign-in token. */
	private User currentUser(Authentication auth) {
		return users.findByEmail(auth.getName());
	}

	
}
