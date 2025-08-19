package org.example.onlinestore.repositories;

import org.example.onlinestore.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR lower(p.name) LIKE lower(concat('%', :name, '%'))) AND " +
           "(:category IS NULL OR lower(p.category) LIKE lower(concat('%', :category, '%')))")
    Page<Product> findByNameAndCategory(@Param("name") String name, @Param("category") String category, Pageable pageable);
}