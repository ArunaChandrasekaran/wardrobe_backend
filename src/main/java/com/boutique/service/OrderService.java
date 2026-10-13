package com.boutique.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.boutique.entity.Order;
import com.boutique.entity.OrderItem;
import com.boutique.entity.Product;
import com.boutique.entity.User;
import com.boutique.repository.OrderRepository;
import com.boutique.repository.ProductRepository;

@Service
public class OrderService {

	private static final int FREE_DELIVERY_ABOVE = 5000;
	private static final int DELIVERY_FEE = 100;

	private final OrderRepository orderRepo;
	private final ProductRepository productRepo;

	OrderService(OrderRepository orderRepo, ProductRepository productRepo) {
		this.orderRepo = orderRepo;
		this.productRepo = productRepo;
	}

	public List<Order> listFor(User user) {
		return orderRepo.findByUserOrderByIdAsc(user);
		// return orderRepo.findByUserOrderByPaymentAsc(user);
		
	}



	public Order findOne(Long id, User user) {
		return orderRepo.findByIdAndUser(id, user)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
	}

	/**
	 * Saves a new order. The page only sends product ids and quantities,
	 * so the price and the total are always worked out here from the database.
	 */
	@SuppressWarnings("unchecked")
	public Order place(User user, Map<String, Object> body) {
		List<Map<String, Object>> lines = (List<Map<String, Object>>) body.get("items");
		String address = text(body.get("address"));

		if (lines == null || lines.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your bag is empty");
		}
		if (address.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Address is required");
		}

		String customerName = text(body.get("customerName"));

		Order order = new Order();
		order.setUser(user);
		order.setCustomerName(customerName.isBlank() ? user.getName() : customerName);
		order.setPhone(text(body.get("phone")));
		order.setAddress(address);
		order.setDeliveryDate(text(body.get("deliveryDate")));
		order.setDeliveryTime(text(body.get("deliveryTime")));
		order.setPayment(text(body.get("payment")));
		order.setStatus("Confirmed");
		order.setPlacedAt(LocalDateTime.now());

		int subTotal = 0;

		for (Map<String, Object> line : lines) {
			int qty = number(line.get("qty")).intValue();
			if (qty < 1) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be at least 1");
			}

			Product product = productRepo.findById(number(line.get("productId")).longValue())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found"));

			if (product.getStock() < qty) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, product.getName() + " is out of stock");
			}
			product.setStock(product.getStock() - qty);
			productRepo.save(product);

			order.getItems().add(newItem(order, product, qty));
			subTotal = subTotal + product.getPrice() * qty;
		}

		int delivery = subTotal >= FREE_DELIVERY_ABOVE ? 0 : DELIVERY_FEE;
		order.setTotal(subTotal + delivery);
		return orderRepo.save(order);
	}

	/** A copy of the product as it was when the order was placed. */
	private OrderItem newItem(Order order, Product product, int qty) {
		OrderItem item = new OrderItem();
		item.setOrder(order);
		item.setProductId(product.getId());
		item.setName(product.getName());
		item.setImage(product.getImage());
		item.setPrice(product.getPrice());
		item.setQty(qty);
		return item;
	}

	private String text(Object value) {
		return value == null ? "" : String.valueOf(value).trim();
	}

	private Number number(Object value) {
		if (value instanceof Number) return (Number) value;
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your bag has a bad item");
	}
}
