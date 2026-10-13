package com.boutique.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.boutique.entity.Order;
import com.boutique.entity.User;

public interface OrderRepository extends JpaRepository<Order, Long> {

	List<Order> findAllByOrderByIdAsc();

	List<Order> findByUserOrderByIdAsc(User user);

	Optional<Order> findByIdAndUser(Long id, User user);

	List<Order> findByUserOrderByPaymentAsc(User user);
}
