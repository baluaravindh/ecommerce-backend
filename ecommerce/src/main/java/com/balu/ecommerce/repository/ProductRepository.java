package com.balu.ecommerce.repository;

import com.balu.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // Find products by category
    List<Product> findByCategory(String category);

    // Find products by name containing a keyword (search)
    List<Product> findByNameContainingIgnoreCase(String keyword);
}
