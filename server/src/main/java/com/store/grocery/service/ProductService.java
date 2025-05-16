package com.store.grocery.service;

import com.store.grocery.domain.Product;
import com.store.grocery.dto.request.product.ProductRequest;
import com.store.grocery.dto.response.PaginationResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.concurrent.CompletableFuture;

public interface ProductService {
    boolean hasProductsInCategory(long categoryId);
    Product create(ProductRequest productRequest);
    void delete(long id);
    PaginationResponse getAll(Specification<Product> spec, Pageable pageable);
    Product findById(long id);
    Product update(long id, ProductRequest productRequest);
    PaginationResponse search(Specification<Product> spec, Pageable pageable);
    CompletableFuture<byte[]> exportDataToExcel();
    Product findByIdAndIsActiveTrue(long id);
}
