package com.balu.ecommerce.specification;

import com.balu.ecommerce.dto.ProductFilterDTO;
import com.balu.ecommerce.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;


public class ProductSpecification {

    public static Specification<Product> buildFilter(ProductFilterDTO filter) {

        return (root, query, criteriaBuilder) -> {

            // List of conditions
            List<Predicate> predicates = new ArrayList<>();

            // FILTER 1: keyword search in name OR description
            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String likePattern = "%" + filter.getKeyword().toLowerCase() + "%";

                Predicate nameLike = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), likePattern);

                Predicate descLike = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), likePattern);

                // keyword matches name OR description
                predicates.add(criteriaBuilder.or(nameLike, descLike));
            }

            // FILTER 2: category exact match
            if (filter.getCategory() != null && !filter.getCategory().isBlank()) {
                predicates.add(
                        criteriaBuilder.equal(root.get("category"), filter.getCategory()));
            }

            // FILTER 3: minimum price
            if (filter.getMinPrice() != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("price"),
                                BigDecimal.valueOf(filter.getMinPrice())));
            }

            // FILTER 4: maximum price
            if (filter.getMaxPrice() != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(root.get("price"),
                                BigDecimal.valueOf(filter.getMaxPrice())));
            }

            // Combine all conditions with AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
