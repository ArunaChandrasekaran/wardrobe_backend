package com.boutique.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.boutique.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

	List<Product> findAllByOrderByIdAsc();

	List<Product> findByCategoryOrderByIdAsc(String category);

	List<Product> findByNewArrivalTrueOrderByIdDesc();
}
