package org.example.onlinestore.services;

import org.example.onlinestore.dtos.ProductResponse;
import org.example.onlinestore.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<ProductResponse> getAllProducts(Pageable pageable);
    ProductResponse getProductById(Long id);
    ProductResponse createProduct(Product product);
    ProductResponse updateProduct(Long id, Product productDetails);
    void deleteProduct(Long id);
    Page<ProductResponse> searchProducts(String name, String category, Pageable pageable);
}